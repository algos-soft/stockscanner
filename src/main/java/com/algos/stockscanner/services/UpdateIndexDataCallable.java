package com.algos.stockscanner.services;

import com.algos.stockscanner.beans.ContextStore;
import com.algos.stockscanner.data.entity.IndexUnit;
import com.algos.stockscanner.data.entity.MarketIndex;
import com.algos.stockscanner.enums.FrequencyTypes;
import com.algos.stockscanner.enums.IndexCategories;
import com.algos.stockscanner.data.service.IndexUnitService;
import com.algos.stockscanner.data.service.MarketIndexService;
import com.algos.stockscanner.enums.IndexUpdateModes;
import com.algos.stockscanner.task.AbortedByUserException;
import com.algos.stockscanner.task.TaskHandler;
import com.algos.stockscanner.task.TaskListener;
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
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
@Scope("prototype")
public class UpdateIndexDataCallable implements Callable<Void> {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private static final DateTimeFormatter fmt = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd")
            .optionalStart()
            .appendPattern(" HH:mm")
            .optionalEnd()
            .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
            .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
            .toFormatter();

    private MarketIndex index;
    private String symbol;
    private IndexUpdateModes mode;
    private LocalDate startDate;
    private ConcurrentLinkedQueue<TaskListener> listeners = new ConcurrentLinkedQueue<>();
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private boolean running;
    private boolean abort;
    private Progress currentProgress;

    private IndexUnit lastValidPoint;

    @Autowired
    private ContextStore contextStore;

    @Autowired
    private IndexUnitService indexUnitService;

    @Autowired
    private MarketIndexService marketIndexService;

    /**
     * @param symbol    the index to update
     * @param mode      the update mode (@see IndexUpdateModes)
     * @param startDate in case of DATE mode, the date when to begin the update
     */
    public UpdateIndexDataCallable(String symbol, IndexUpdateModes mode, LocalDate startDate) {
        this.symbol = symbol;
        this.mode = mode;
        this.startDate = startDate;
    }


    @PostConstruct
    private void init() {

        log.info("Callable task created for index " + symbol);

        // register itself to the context-level storage
        contextStore.updateIndexCallableMap.put("" + hashCode(), this);

        try {
            index=marketIndexService.findUniqueBySymbol(symbol);
        } catch (Exception e) {
            log.error("symbol not found: "+symbol, e);
        }

        currentProgress=new Progress();

        // puts the task in 'waiting for start' status
        currentProgress.update("waiting...");

    }

    @Override
    public Void call() {

        // if already aborted before starting,
        // unregister itself from the context-level storage and return
        if(abort){
            contextStore.updateIndexCallableMap.remove("" + hashCode(), this);
            return null;
        }

        log.debug("Callable task called for index " + symbol);

        running=true;

        // if is already aborted, don't perform the task
        startTime = LocalDateTime.now();

        // long task, can throw exception
        try {

            checkAbort();   // throws exception if the task is aborted

            // retrieve the category
            java.util.Optional<IndexCategories> oCategory = IndexCategories.getItem(index.getCategory());
            if (!oCategory.isPresent()) {
                throw new Exception("Index " + symbol + " does not have a category.");
            }
            IndexCategories category = oCategory.get();

            // switch on the category
            String url = null;
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

                    notifyProgress(0, 0, "Requesting data");

                    TimeSeriesResponse response=executeRequest();

                    String error=response.getErrorMessage();
                    if(error!=null){
                        throw new Exception(error);
                    }

                    handleResponse(response);

                    break;
                case TECH:
                    break;
            }

            endTime = LocalDateTime.now();
            String info = buildDurationInfo();
            log.info("Callable task completed for index " + symbol + " " + info);
            notifyCompleted(info);

        } catch (Exception e) {

            terminateWithError(e);

        } finally {

            // unregister itself from the context-level storage
            contextStore.updateIndexCallableMap.remove("" + hashCode(), this);

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
    private TimeSeriesResponse executeRequest(){

        OutputSize outputSize=null;
        switch (mode){
            case ALL_DATA:
                outputSize=OutputSize.FULL;
                break;
            case MISSING_DATA:
                outputSize=OutputSize.COMPACT;
                break;
        }

        TimeSeriesResponse response=AlphaVantage.api()
                .timeSeries()
                .daily()
                .forSymbol(symbol)
                .outputSize(outputSize)
                .dataType(DataType.JSON)
                .fetchSync();

        if(mode.equals(IndexUpdateModes.MISSING_DATA)){
            if(!coversMissingPeriod(response)){
                response=AlphaVantage.api()
                        .timeSeries()
                        .daily()
                        .forSymbol(symbol)
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
    private boolean coversMissingPeriod(TimeSeriesResponse response){
        List<StockUnit> units = response.getStockUnits();
        if(units.size()>0){
            Collections.sort(units, Comparator.comparing(StockUnit::getDate));
            StockUnit firstUnit= units.get(0);
            LocalDateTime firstPoint = LocalDateTime.parse(firstUnit.getDate(), fmt);
            List<IndexUnit> unitsEqOrPost = indexUnitService.findAllByIndexWithDateTimeEqualOrAfterOrderByDate(index, firstPoint);
            if(unitsEqOrPost.size()>0){
                lastValidPoint = unitsEqOrPost.get(unitsEqOrPost.size()-1); // last valid unit present in db, save it for later
                return true;
            }
        }
        return false;
    }


    private String buildDurationInfo() {
        Duration duration = Duration.between(startTime, endTime);
        String sDuration = DurationFormatUtils.formatDuration(duration.toMillis(), "H:mm:ss", true);
        DateTimeFormatter format = DateTimeFormatter.ofPattern("HH:mm:ss");
        String info = "start: " + startTime.format(format) + ", end: " + endTime.format(format) + ", elapsed: " + sDuration;
        return info;
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
        TaskHandler handler = new TaskHandler() {
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
        return handler;
    }


    private void notifyProgress(int current, int tot, String info) {

        currentProgress.update(current, tot, info);

        for (TaskListener listener : listeners) {
            listener.onProgress(current, tot, info);
        }
    }

    private void notifyError(Exception e) {
        for (TaskListener listener : listeners) {
            listener.onError(e);
        }
    }

    private void notifyCompleted(String info) {
        for (TaskListener listener : listeners) {
            listener.onCompleted(info);
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
    public void handleResponse(TimeSeriesResponse response)  throws Exception {

        // if replace all, delete all previous unit data
        if(mode.equals(IndexUpdateModes.ALL_DATA)){
            notifyProgress(0, 0, "Deleting old data");
            indexUnitService.deleteByIndex(index);
        }

        // Iterate the new units and save them
        List<StockUnit> units = response.getStockUnits();
        Collections.sort(units, Comparator.comparing(StockUnit::getDate));
        int j = 0;
        LocalDateTime minDateTime = null;
        LocalDateTime maxDateTime = null;
        for (StockUnit unit : units) {

            checkAbort();

            j++;

            notifyProgress(j, units.size(), symbol);


            if(mode.equals(IndexUpdateModes.MISSING_DATA)){
//                if(lastValidPoint....){
//                    continue;
//                }
            }

            IndexUnit indexUnit = saveItem(unit);

            // keep minDateTime and maxDateTime up to date
            if (minDateTime == null) {
                minDateTime = indexUnit.getDateTimeLDT();
            } else {
                if (indexUnit.getDateTimeLDT().isBefore(minDateTime)) {
                    minDateTime = indexUnit.getDateTimeLDT();
                }
            }
            if (maxDateTime == null) {
                maxDateTime = indexUnit.getDateTimeLDT();
            } else {
                if (indexUnit.getDateTimeLDT().isAfter(maxDateTime)) {
                    maxDateTime = indexUnit.getDateTimeLDT();
                }
            }

        }

        // Consolidate the totals in the MarketIndex
        index.setUnitsFromLD(minDateTime.toLocalDate());
        index.setUnitsToLD(maxDateTime.toLocalDate());
        index.setNumUnits(units.size());
        index.setUnitFrequency(FrequencyTypes.DAILY.getCode());
        index.setPricesUpdateTs(Du.toUtcString(LocalDateTime.now()));
        marketIndexService.update(index);

    }


    private IndexUnit saveItem(StockUnit unit) {
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
            log.info("Callable task aborted by user for index " + symbol);
        }else{
            log.error("Callable task error for index " + symbol, e);
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
