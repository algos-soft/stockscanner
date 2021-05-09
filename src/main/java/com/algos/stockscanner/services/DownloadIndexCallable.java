package com.algos.stockscanner.services;

import com.algos.stockscanner.Application;
import com.algos.stockscanner.beans.ContextStore;
import com.algos.stockscanner.beans.Utils;
import com.algos.stockscanner.data.entity.IndexUnit;
import com.algos.stockscanner.data.entity.MarketIndex;
import com.algos.stockscanner.enums.FrequencyTypes;
import com.algos.stockscanner.enums.IndexCategories;
import com.algos.stockscanner.data.service.IndexUnitService;
import com.algos.stockscanner.data.service.MarketIndexService;
import com.algos.stockscanner.enums.IndexDownloadModes;
import com.algos.stockscanner.task.AbortedByUserException;
import com.algos.stockscanner.task.TaskHandler;
import com.algos.stockscanner.task.TaskListener;
import com.crazzyghost.alphavantage.AlphaVantage;
import com.crazzyghost.alphavantage.parameters.DataType;
import com.crazzyghost.alphavantage.parameters.OutputSize;
import com.crazzyghost.alphavantage.timeseries.response.StockUnit;
import com.crazzyghost.alphavantage.timeseries.response.TimeSeriesResponse;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.vaadin.flow.component.html.Image;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.time.Duration;
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
public class DownloadIndexCallable implements Callable<Void> {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private static final DateTimeFormatter fmt = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd")
            .optionalStart()
            .appendPattern(" HH:mm")
            .optionalEnd()
            .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
            .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
            .toFormatter();

    private IndexDownloadModes mode;
    private String symbol;
    private ConcurrentLinkedQueue<TaskListener> listeners = new ConcurrentLinkedQueue<>();
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private boolean running;
    private boolean abort;
    private Progress currentProgress;

    @Autowired
    private ContextStore contextStore;

    @Autowired
    private IndexUnitService indexUnitService;

    @Autowired
    private MarketIndexService marketIndexService;

    @Autowired
    private Utils utils;

    @Value("${alphavantage.api.key}")
    private String alphavantageApiKey;

    private OkHttpClient okHttpClient;

    private JsonAdapter<MarketService.FDResponse> fdJsonAdapter;


    /**
     * @param mode      the download mode
     * @param symbol    the symbol to download/update
     */
    public DownloadIndexCallable(IndexDownloadModes mode, String symbol) {
        this.mode = mode;
        this.symbol=symbol;
    }

    @PostConstruct
    private void init() {

        log.info("Callable task created for index " + symbol);

        // register itself to the context-level storage
        contextStore.downloadIndexCallableMap.put("" + hashCode(), this);

        currentProgress=new Progress();

        // puts the task in 'waiting for start' status
        currentProgress.update("waiting...");

        okHttpClient = new OkHttpClient();
        Moshi moshi = new Moshi.Builder().build();
        fdJsonAdapter = moshi.adapter(MarketService.FDResponse.class);

    }

    @Override
    public Void call() {

        // if already aborted before starting,
        // unregister itself from the context-level storage and return
        if(abort){
            contextStore.downloadIndexCallableMap.remove("" + hashCode(), this);
            return null;
        }

        log.debug("Download task called for index " + symbol);

        running=true;

        // if is already aborted, don't perform the task
        startTime = LocalDateTime.now();

        // long task, can throw exception
        try {

            checkAbort();   // throws exception if the task is aborted

            notifyProgress(1, 3, "Requesting data");

            FundamentalData fundamentalData = fetchFundamentalData(symbol);

            handleResponse(fundamentalData);

            endTime = LocalDateTime.now();
            String info = buildDurationInfo();
            log.info("Download task completed for index " + symbol + " " + info);
            notifyCompleted(info);

        } catch (Exception e) {

            terminateWithError(e);

        } finally {

            // unregister itself from the context-level storage
            contextStore.downloadIndexCallableMap.remove("" + hashCode(), this);

        }



        return null;

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
     * Fetch fundamental data from the network
     */
    private FundamentalData fetchFundamentalData(String symbol) throws IOException {
        FundamentalData fundamentalData=null;

        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://www.alphavantage.co/query").newBuilder();
        urlBuilder.addQueryParameter("apikey", alphavantageApiKey);
        urlBuilder.addQueryParameter("function", "OVERVIEW");
        urlBuilder.addQueryParameter("symbol", symbol);

        String url = urlBuilder.build().toString();

        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = okHttpClient.newCall(request).execute();
        if(response.isSuccessful()){

            MarketService.FDResponse fdresp = fdJsonAdapter.fromJson(response.body().string());

            if(fdresp.Symbol==null){
                throw new IOException("malformed response, license limits reached?");
            }

            IndexCategories category = IndexCategories.getByAlphaVantageType(fdresp.AssetType);
            fundamentalData=new FundamentalData(symbol, fdresp.Name, category);

        }

        return fundamentalData;
    }


//    /**
//     * Manage a successful response from the api
//     */
//    public void handleResponse(TimeSeriesResponse response)  throws Exception {
//
//        // delete all previous unit data
//        notifyProgress(0, 0, "Deleting old data");
//        indexUnitService.deleteByIndex(index);
//
//        // Iterate the new units and save them
//        List<StockUnit> units = response.getStockUnits();
//        Collections.sort(units, Comparator.comparing(StockUnit::getDate));
//        int j = 0;
//        LocalDateTime minDateTime = null;
//        LocalDateTime maxDateTime = null;
//        for (StockUnit unit : units) {
//
//            checkAbort();
//
//            j++;
//
//            notifyProgress(j, units.size(), index.getSymbol());
//
//            IndexUnit indexUnit = saveItem(unit);
//
//            // keep minDateTime and maxDateTime up to date
//            if (minDateTime == null) {
//                minDateTime = indexUnit.getDateTimeLDT();
//            } else {
//                if (indexUnit.getDateTimeLDT().isBefore(minDateTime)) {
//                    minDateTime = indexUnit.getDateTimeLDT();
//                }
//            }
//            if (maxDateTime == null) {
//                maxDateTime = indexUnit.getDateTimeLDT();
//            } else {
//                if (indexUnit.getDateTimeLDT().isAfter(maxDateTime)) {
//                    maxDateTime = indexUnit.getDateTimeLDT();
//                }
//            }
//
//        }
//
//        // Consolidate the totals in the MarketIndex
//        index.setUnitsFromLD(minDateTime.toLocalDate());
//        index.setUnitsToLD(maxDateTime.toLocalDate());
//        index.setNumUnits(units.size());
//        index.setUnitFrequency(FrequencyTypes.DAILY.getCode());
//        marketIndexService.update(index);
//
//    }



     /**
     * Manage a successful response from the api
     * find the index in the database.
     * If exists, update it
     * If not, create it
     */
    private void handleResponse(FundamentalData fd) throws Exception {

        notifyProgress(2, 3, "updating db: "+symbol);

        List<MarketIndex> indexes = marketIndexService.findBySymbol(fd.getSymbol());
        if(indexes.size()>1){
            throw new Exception("Multiple instances ("+indexes.size()+") of symbol "+fd.getSymbol()+" found in the database.");
        }

        MarketIndex index;
        if(indexes.size()==0){  // does not exist in db
            index=new MarketIndex();
            index.setSymbol(fd.getSymbol());
            index.setName(fd.getName());
            index.setCategory(fd.getType().getCode());
            updateIcon(index);

        }else{  // index exists in db
            index = indexes.get(0);
            if(fd.getName()!=null){
                index.setName(fd.getName());
            }
            if(fd.getType()!=null){
                index.setCategory(fd.getType().getCode());
            }

        }

        marketIndexService.update(index);

        notifyProgress(3, 3, "done: "+symbol);

    }


    /**
     * add the icon to an index if missing
     */
    private void updateIcon(MarketIndex index) throws IOException {

        if(index.getImage()==null){
            String url = "https://etoro-cdn.etorostatic.com/market-avatars/"+index.getSymbol().toLowerCase()+"/150x150.png";
            byte[] bytes = utils.getBytesFromUrl(url);
            if(bytes!=null){
                if(bytes.length>0){
                    byte[] scaled = utils.scaleImage(bytes, Application.STORED_ICON_WIDTH, Application.STORED_ICON_HEIGHT);
                    index.setImage(scaled);
                }
            }else{  // Icon not found on etoro, symbol not managed on eToro or icon has a different name? Use standard icon.
                bytes = utils.getDefaultIndexIconBytes();
                byte[] scaled = utils.scaleImage(bytes, Application.STORED_ICON_WIDTH, Application.STORED_ICON_HEIGHT);
                index.setImage(scaled);
            }
        }

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
            notifyProgress(currentProgress.getCurrent(), currentProgress.getTot(), "Error");
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
