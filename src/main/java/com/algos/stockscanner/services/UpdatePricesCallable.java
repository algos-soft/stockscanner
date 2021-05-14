package com.algos.stockscanner.services;

import com.algos.stockscanner.beans.ContextStore;
import com.algos.stockscanner.data.entity.IndexUnit;
import com.algos.stockscanner.data.entity.MarketIndex;
import com.algos.stockscanner.enums.FrequencyTypes;
import com.algos.stockscanner.enums.IndexCategories;
import com.algos.stockscanner.data.service.IndexUnitService;
import com.algos.stockscanner.data.service.MarketIndexService;
import com.algos.stockscanner.enums.PriceUpdateModes;
import com.algos.stockscanner.task.AbortedByUserException;
import com.algos.stockscanner.task.TaskHandler;
import com.algos.stockscanner.task.TaskListener;
import com.algos.stockscanner.utils.CpuMonitorListener;
import com.algos.stockscanner.utils.CpuMonitorTask;
import com.algos.stockscanner.utils.Du;
import com.crazzyghost.alphavantage.AlphaVantage;
import com.crazzyghost.alphavantage.parameters.DataType;
import com.crazzyghost.alphavantage.parameters.OutputSize;
import com.crazzyghost.alphavantage.timeseries.response.StockUnit;
import com.crazzyghost.alphavantage.timeseries.response.TimeSeriesResponse;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
@Scope("prototype")
public class UpdatePricesCallable implements Callable<Void> {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private static final DateTimeFormatter fmt = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd")
            .optionalStart()
            .appendPattern(" HH:mm")
            .optionalEnd()
            .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
            .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
            .toFormatter();

    private final List<String> symbols;
    private PriceUpdateModes mode;
    private final int maxReqPerMinute;

    private final ConcurrentLinkedQueue<TaskListener> listeners = new ConcurrentLinkedQueue<>();
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private boolean running;
    private boolean abort;
    private Progress currentProgress;

    private LocalDateTime lastValidPoint;
    private LocalDateTime lastRequestTs;
    private int minMillisBetweenReq;
    private int symbolCount=0;

    private TimerTask cpuMonitor;
    private int cpuPauseMs;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private ContextStore contextStore;

    @Autowired
    private IndexUnitService indexUnitService;

    @Autowired
    private MarketIndexService marketIndexService;


    /**
     * @param symbols    the list of indexes to update
     * @param mode      the update mode (@see IndexUpdateModes)
     * @param maxReqPerMinute the max number of request per minute
     */
    public UpdatePricesCallable(List<String> symbols, PriceUpdateModes mode, int maxReqPerMinute) {
        this.symbols = symbols;
        this.mode = mode;
        this.maxReqPerMinute=maxReqPerMinute;
    }


    @PostConstruct
    private void init() {

        log.info("Update prices task created for "+symbols);

        // register itself to the context-level storage
        contextStore.updateIndexCallableMap.put("" + hashCode(), this);

        currentProgress=new Progress();

        minMillisBetweenReq=60/maxReqPerMinute*1000;
        minMillisBetweenReq=(int)(minMillisBetweenReq*1.05); // security margin

        // puts the task in 'waiting for start' status
        currentProgress.update("waiting...");

        // start a thread to monitor the CPU load
        cpuMonitor = context.getBean(CpuMonitorTask.class, (CpuMonitorListener) delayMs -> {
            cpuPauseMs =delayMs;
        });

    }

    @Override
    public Void call() {

        // if already aborted before starting,
        // unregister itself from the context-level storage and return
        if(abort){
            contextStore.updateIndexCallableMap.remove("" + hashCode(), this);
            return null;
        }

        log.debug("Update prices task called for "+symbols);

        running=true;

        notifyStarted(null);

        // if is already aborted, don't perform the task
        startTime = LocalDateTime.now();

        // long task, can throw exception
        try {

            for(String symbol : symbols){

                symbolCount++;

                MarketIndex index = marketIndexService.findUniqueBySymbol(symbol);

                checkAbort();   // throws exception if the task is aborted

                // retrieve the category
                java.util.Optional<IndexCategories> oCategory = IndexCategories.getItem(index.getCategory());
                if (!oCategory.isPresent()) {
                    throw new Exception("Index " + symbol + " does not have a category.");
                }
                IndexCategories category = oCategory.get();

                // switch on the category
                switch (category) {
                    case CRYPTO:
                        break;
                    case EXCHANGE:
                        break;
                    case FOREX:
                        break;
                    case SECTOR:
                        break;
                    case STOCK:

                        TimeSeriesResponse response=executeRequest(index);

                        String error=response.getErrorMessage();
                        if(error!=null){
                            throw new Exception(error);
                        }

                        handleResponse(response, index);

                        break;
                    case TECH:
                        break;
                }

            }

            log.info("Update prices task completed for " + symbols);

            endTime = LocalDateTime.now();
            String info = buildDurationInfo();
            notifyCompleted(info);

        } catch (Exception e) {

            terminateWithError(e);

        } finally {

            // unregister itself from the context-level storage
            contextStore.updateIndexCallableMap.remove("" + hashCode(), this);

            // cancel the CPU timer
            if(cpuMonitor !=null){
                cpuMonitor.cancel();
            }

        }

        return null;

    }


    /**
     * Execute the request synchronously and return the response.
     * <br>
     * If the request mode is ALL_DATA, makes a full request and returns the result.
     * If the request mode is MISSING_DATA, makes a compact request and checks if
     * the returned data is enough to cover the missing period.
     * If the check is positive, return the result.
     * If negative, make a full request and return the result.
     */
    private TimeSeriesResponse executeRequest(MarketIndex index){

        // check request frequency and eventually pause for a while
        if(lastRequestTs!=null){
            int millisElapsed = (int)lastRequestTs.until(LocalDateTime.now(), ChronoUnit.MILLIS);
            if(millisElapsed<minMillisBetweenReq){
                int millisToWait=minMillisBetweenReq-millisElapsed;
                try {
                    notifyProgress(0,0,index.getSymbol() + " paused for timing");
                    Thread.sleep(millisToWait);
                } catch (InterruptedException e) {
                    log.error("timing error", e);
                }
            }
        }

        notifyProgress(0,0,"Requesting data for "+index.getSymbol());
        log.info("Requesting data for "+index.getSymbol());

        OutputSize outputSize=null;
        switch (mode){
            case REPLACE_ALL_DATA:
                outputSize=OutputSize.FULL;
                break;
            case ADD_MISSING_DATA_ONLY:
                outputSize=OutputSize.COMPACT;
                break;
        }

        lastRequestTs=LocalDateTime.now();

        TimeSeriesResponse response=AlphaVantage.api()
                .timeSeries()
                .daily()
                .forSymbol(index.getSymbol())
                .outputSize(outputSize)
                .dataType(DataType.JSON)
                .fetchSync();

        if(mode.equals(PriceUpdateModes.ADD_MISSING_DATA_ONLY)){
            if(!coversMissingPeriod(response, index)){

                // the compact packet does not cover the missing period!
                // switch automatically to REPLACE_ALL_DATA mode
                // and perform the full query
                mode= PriceUpdateModes.REPLACE_ALL_DATA;

                response=AlphaVantage.api()
                        .timeSeries()
                        .daily()
                        .forSymbol(index.getSymbol())
                        .outputSize(OutputSize.FULL)
                        .dataType(DataType.JSON)
                        .fetchSync();
            }
        }

        return response;
    }


    /**
     * Check if the data contained in the response covers the missing data for the index.
     * The first point of the data contained in the response must precede or be equal to the
     * last point currently present for the symbol.
     */
    private boolean coversMissingPeriod(TimeSeriesResponse response, MarketIndex index){
        List<StockUnit> units = response.getStockUnits();
        if(units.size()>0){
            Collections.sort(units, Comparator.comparing(StockUnit::getDate));
            StockUnit firstUnit= units.get(0);
            LocalDateTime firstPoint = LocalDateTime.parse(firstUnit.getDate(), fmt);
            List<IndexUnit> unitsEqOrPost = indexUnitService.findAllByIndexWithDateTimeEqualOrAfterOrderByDate(index, firstPoint);
            if(unitsEqOrPost.size()>0){
                IndexUnit lastValidUnit = unitsEqOrPost.get(unitsEqOrPost.size()-1); // last valid unit present in db, save it for later
                lastValidPoint = lastValidUnit.getDateTimeLDT();
                return true;
            }
        }
        return false;
    }


    private String buildDurationInfo() {
        Duration duration = Duration.between(startTime, endTime);
        String sDuration = DurationFormatUtils.formatDuration(duration.toMillis(), "H:mm:ss", true);
        DateTimeFormatter format = DateTimeFormatter.ofPattern("HH:mm:ss");
        return "start: " + startTime.format(format) + ", end: " + endTime.format(format) + ", elapsed: " + sDuration;
    }

    /**
     * @param listener listener to inform with progress events
     */
    public void addListener(TaskListener listener) {
        this.listeners.add(listener);

        // update the listener as soon as it attaches
        listener.onProgress(currentProgress.getCurrent(), currentProgress.getTot(), currentProgress.getStatus());

    }


    /**
     * @return provide a handler to interrupt/manage the execution
     */
    public TaskHandler obtainHandler() {
        return new TaskHandler() {
            @Override
            public void abort() {

                // turn on the abort flag
                abort=true;

                // if not running yet (the task is scheduled and is in a wait state)
                // call immediately the abort procedure
                if(!running){
                    try {
                        checkAbort();
                    } catch (Exception e) {
                        terminateWithError(e);
                    }
                }
            }
        };
    }

    private void notifyStarted(String info) {
        for (TaskListener listener : listeners) {
            // anything that happens here, must not stop the execution
            try {
                listener.onStarted(info);
            }catch (Exception e){
                log.debug("Could not notify the start listener", e);
            }
        }
    }


    private void notifyProgress(int current, int tot, String info) {

        currentProgress.update(current, tot, info);

        for (TaskListener listener : listeners) {
            String pre = "["+symbolCount+"/"+symbols.size()+"]";
            // anything that happens here, must not stop the execution
            try {
                listener.onProgress(current, tot, pre+" "+info);
            }catch (Exception e){
                log.debug("Could not notify the progress listener", e);
            }
        }
    }

    private void notifyError(Exception e) {
        for (TaskListener listener : listeners) {
            // anything that happens here, must not stop the execution
            try {
                listener.onError(e);
            }catch (Exception e1){
                log.debug("Could not notify the error listener", e1);
            }
        }
    }

    private void notifyCompleted(String info) {
        for (TaskListener listener : listeners) {
            // anything that happens here, must not stop the execution
            try {
                listener.onCompleted(info);
            }catch (Exception e){
                log.debug("Could not notify the completed listener", e);
            }
        }
    }


    private void checkAbort() throws Exception{
        if (abort) {
            currentProgress.update("Aborted");
            notifyProgress(currentProgress.getCurrent(), currentProgress.getTot(), currentProgress.getStatus());
            throw new AbortedByUserException();
        }
    }


    /**
     * Manage a successful response from the api
     */
    public void handleResponse(TimeSeriesResponse response, MarketIndex index)  throws Exception {

        log.info("Processing data for "+index.getSymbol());

        // if we are in REPLACE_ALL_DATA mode, delete all previous data
        if(mode.equals(PriceUpdateModes.REPLACE_ALL_DATA)){
            notifyProgress(0, 0, "Deleting old data for "+index.getSymbol());
            indexUnitService.deleteByIndex(index);
        }

        // Iterate the new units and save them
        List<StockUnit> units = response.getStockUnits();
        Collections.sort(units, Comparator.comparing(StockUnit::getDate));
        int j = 0;
        int countUnits=0;
        for (StockUnit unit : units) {

            // apply retroaction to cpu load
            if(cpuPauseMs >0){
                Thread.sleep(cpuPauseMs);
            }

            checkAbort();

            j++;

            notifyProgress(j, units.size(), index.getSymbol());

            // if we are in ADD_MISSING_DATA_ONLY mode, skip the points
            // before or equal the lastValidPoint
            if(mode.equals(PriceUpdateModes.ADD_MISSING_DATA_ONLY)){
                LocalDateTime dateTime = LocalDateTime.parse(unit.getDate(), fmt);
                if(!dateTime.isAfter(lastValidPoint)){
                    continue;
                }
            }

            saveItem(unit, index);
            countUnits++;

        }

        log.info("Processing completed for "+index.getSymbol()+ " ("+countUnits+" units added)");

        // Consolidate the totals in the MarketIndex
        IndexUnit unit;
        unit = indexUnitService.findFirstByDate(index);
        if(unit!=null){
            index.setUnitsFromLD(unit.getDateTimeLDT().toLocalDate());
        }
        unit = indexUnitService.findLastByDate(index);
        if(unit!=null){
            index.setUnitsToLD(unit.getDateTimeLDT().toLocalDate());
        }
        index.setNumUnits(indexUnitService.countBy(index));
        index.setUnitFrequency(FrequencyTypes.DAILY.getCode());
        index.setPricesUpdateTs(Du.toUtcString(LocalDateTime.now()));
        marketIndexService.update(index);

    }

    public static double getProcessCpuLoad() throws Exception {

        MBeanServer mbs    = ManagementFactory.getPlatformMBeanServer();
        ObjectName name    = ObjectName.getInstance("java.lang:type=OperatingSystem");
        AttributeList list = mbs.getAttributes(name, new String[]{ "ProcessCpuLoad" });

        if (list.isEmpty())     return Double.NaN;

        Attribute att = (Attribute)list.get(0);
        Double value  = (Double)att.getValue();

        // usually takes a couple of seconds before we get real values
        if (value == -1.0)      return Double.NaN;
        // returns a percentage value with 1 decimal point precision
        return ((int)(value * 1000) / 10.0);
    }



    private IndexUnit saveItem(StockUnit unit, MarketIndex index) {
        IndexUnit indexUnit = createIndexUnit(unit);
        indexUnit.setIndex(index);
        return indexUnitService.update(indexUnit);
    }

    private IndexUnit createIndexUnit(StockUnit unit) {
        IndexUnit indexUnit = new IndexUnit();
        indexUnit.setOpen((float) unit.getOpen());
        indexUnit.setClose((float) unit.getClose());

        LocalDateTime dateTime = LocalDateTime.parse(unit.getDate(), fmt);

        indexUnit.setDateTimeLDT(dateTime);

        return indexUnit;
    }


    /**
     * Sets the end timestamp, logs the error and
     * notifies the listeners
     */
    private void terminateWithError(Exception e){
        endTime = LocalDateTime.now();
        if(e instanceof AbortedByUserException){
            log.info("Callable task aborted by user");
        }else{
            log.error("Callable task error", e);
        }
        notifyError(e);
    }


    class Progress{
        private int current;
        private int tot;
        private String status;

        public void update(int current, int tot, String status){
            this.current=current;
            this.tot=tot;
            this.status=status;
        }

        public void update(String status){
            this.status=status;
        }

        public int getCurrent() {
            return current;
        }

        public int getTot() {
            return tot;
        }

        public String getStatus() {
            return status;
        }
    }


}
