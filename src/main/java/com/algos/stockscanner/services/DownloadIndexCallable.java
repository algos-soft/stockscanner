package com.algos.stockscanner.services;

import com.algos.stockscanner.Application;
import com.algos.stockscanner.beans.ContextStore;
import com.algos.stockscanner.beans.Utils;
import com.algos.stockscanner.data.entity.MarketIndex;
import com.algos.stockscanner.data.service.MarketIndexService;
import com.algos.stockscanner.enums.IndexCategories;
import com.algos.stockscanner.task.AbortedByUserException;
import com.algos.stockscanner.task.TaskHandler;
import com.algos.stockscanner.task.TaskListener;
import com.algos.stockscanner.utils.CpuMonitorListener;
import com.algos.stockscanner.utils.CpuMonitorTask;
import com.algos.stockscanner.utils.Du;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
@Scope("prototype")
public class DownloadIndexCallable implements Callable<Void> {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private String symbol;
    private ConcurrentLinkedQueue<TaskListener> listeners = new ConcurrentLinkedQueue<>();
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private boolean running;
    private boolean abort;
    private Progress currentProgress;

    @Autowired
    private ContextStore contextStore;

//    @Autowired
//    private IndexUnitService indexUnitService;

    @Autowired
    private MarketIndexService marketIndexService;

    @Autowired
    private Utils utils;

    @Autowired
    private ApplicationContext context;

    @Value("${alphavantage.api.key}")
    private String alphavantageApiKey;

    private OkHttpClient okHttpClient;

    private JsonAdapter<MarketService.FDResponse> fdJsonAdapter;


    /**
     * @param symbol    the symbol to download/update
     */
    public DownloadIndexCallable(String symbol) {
        this.symbol=symbol;
    }

    @PostConstruct
    private void init() {

        log.info("Download task created for index " + symbol);

        // register itself to the context-level storage
        contextStore.downloadIndexCallableMap.put("" + hashCode(), this);

        currentProgress= new Progress();

        // puts the task in 'waiting for start' status
        currentProgress.update("waiting: "+symbol);

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
        TaskHandler handler = () -> {

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
            currentProgress.update("Aborted: "+symbol);
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
            fundamentalData=new FundamentalData(symbol, fdresp.Name, category, fdresp.Exchange, fdresp.Country, fdresp.Sector, fdresp.Industry, fdresp.MarketCapitalization, fdresp.EBITDA);

        }

        return fundamentalData;
    }




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
            updateIcon(index);

        }else{  // index exists in db
            index = indexes.get(0);
        }

        index.setName(fd.getName());
        index.setCategory(fd.getType().getCode());
        index.setExchange(fd.getExchange());
        index.setCountry(fd.getCountry());
        index.setSector(fd.getSector());
        index.setIndustry(fd.getIndustry());
        index.setMarketCap(fd.getMarketCap());
        index.setEbitda(fd.getEbitda());

        index.setFundamentalUpdateTs(Du.toUtcString(LocalDateTime.now()));

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
            log.info("Download task aborted by user for index " + symbol);
        }else{
            notifyProgress(currentProgress.getCurrent(), currentProgress.getTot(), "Error: "+symbol);
            log.error("Download task error for index " + symbol, e);
        }
        notifyError(e);
    }


    static class Progress{
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
