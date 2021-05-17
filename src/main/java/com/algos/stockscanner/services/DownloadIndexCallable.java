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
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
@Scope("prototype")
public class DownloadIndexCallable implements Callable<Void> {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final List<String> symbols;
    private final int maxReqPerMinute;

    private ConcurrentLinkedQueue<TaskListener> listeners = new ConcurrentLinkedQueue<>();
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private boolean running;
    private boolean abort;
    private Progress currentProgress;

    private LocalDateTime lastRequestTs;
    private int minMillisBetweenReq;
    private int symbolCount=0;


    @Autowired
    private ContextStore contextStore;

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
     * @param symbols the list of indexes to download/update
     * @param maxReqPerMinute the max number of request per minute
     */
    public DownloadIndexCallable(List<String> symbols, int maxReqPerMinute) {
        this.symbols=symbols;
        this.maxReqPerMinute=maxReqPerMinute;
    }

    @PostConstruct
    private void init() {

        log.info("Download task created for "+symbols);

        // register itself to the context-level storage
        contextStore.downloadIndexCallableMap.put("" + hashCode(), this);

        currentProgress= new Progress();

        // puts the task in 'waiting for start' status
        currentProgress.update("waiting...");

        minMillisBetweenReq=60/maxReqPerMinute*1000;
        minMillisBetweenReq=(int)(minMillisBetweenReq*1.05); // security margin

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

        log.debug("Download task called for "+symbols);

        running=true;

        notifyStarted(null);

        // if is already aborted, don't perform the task
        startTime = LocalDateTime.now();

        // long task, can throw exception
        try {

            for(String symbol : symbols){

                symbolCount++;

                checkAbort();   // throws exception if the task is aborted

                notifyProgress(1, 3, "Requesting data");

                FundamentalData fundamentalData = fetchFundamentalData(symbol);

                if(fundamentalData!=null){
                    handleResponse(fundamentalData, symbol);
                }

            }

            log.info("Download task completed for "+symbols);

            endTime = LocalDateTime.now();
            String info = buildDurationInfo();

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

    private void notifyStarted(String info) {
        for (TaskListener listener : listeners) {
            try {
                listener.onStarted(info);
            }catch (Exception e){
                log.debug("Could not notify the start listener", e);
            }
        }
    }


    private void notifyProgress(int current, int tot, String info) {

        currentProgress.update(current, tot, info);

        String pre = "["+symbolCount+"/"+symbols.size()+"]";

        for (TaskListener listener : listeners) {
            try {
                listener.onProgress(current, tot, pre+" "+info);
            }catch (Exception e){
                log.debug("Could not notify the progress listener", e);
            }
        }
    }

    private void notifyError(Exception e) {
        for (TaskListener listener : listeners) {
            try {
                listener.onError(e);
            }catch (Exception e1){
                log.debug("Could not notify the error listener", e1);
            }
        }
    }

    private void notifyCompleted(String info) {
        for (TaskListener listener : listeners) {
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
     * Fetch fundamental data from the network
     */
    private FundamentalData fetchFundamentalData(String symbol) throws IOException {

        // check request frequency and eventually pause for a while
        if(lastRequestTs!=null){
            int millisElapsed = (int)lastRequestTs.until(LocalDateTime.now(), ChronoUnit.MILLIS);
            if(millisElapsed<minMillisBetweenReq){
                int millisToWait=minMillisBetweenReq-millisElapsed;
                try {
                    notifyProgress(0,0,symbol+" paused for timing");
                    Thread.sleep(millisToWait);
                } catch (InterruptedException e) {
                    log.error("timing error", e);
                }
            }
        }

        FundamentalData fundamentalData=null;

        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://www.alphavantage.co/query").newBuilder();
        urlBuilder.addQueryParameter("apikey", alphavantageApiKey);
        urlBuilder.addQueryParameter("function", "OVERVIEW");
        urlBuilder.addQueryParameter("symbol", symbol);

        String url = urlBuilder.build().toString();

        Request request = new Request.Builder()
                .url(url)
                .build();

        lastRequestTs=LocalDateTime.now();

        Response response = okHttpClient.newCall(request).execute();
        if(response.isSuccessful()){

            String respString=response.body().string();

            if (respString.contains("Thank you")) {   // limit reached
                log.error("Alphavantage limit reached: " + respString);
                throw new LimitReachedException();
            }else{

                MarketService.FDResponse fdresp = fdJsonAdapter.fromJson(respString);

                if(fdresp.Symbol==null){
                    throw new IOException("malformed response for "+symbol+", license limits reached? resp="+respString);
                }

                IndexCategories category = IndexCategories.getByAlphaVantageType(fdresp.AssetType);
                long marketCap=0;
                try {
                    marketCap=Long.parseLong(fdresp.MarketCapitalization);
                }catch (Exception e){
                }
                long ebitda=0;
                try {
                    ebitda=Long.parseLong(fdresp.EBITDA);
                }catch (Exception e){
                }
                fundamentalData=new FundamentalData(symbol, fdresp.Name, category, fdresp.Exchange, fdresp.Country, fdresp.Sector, fdresp.Industry, marketCap, ebitda);

            }


        }

        return fundamentalData;
    }


    public class LimitReachedException extends Exception {

    }




     /**
     * Manage a successful response from the api
     * find the index in the database.
     * If exists, update it
     * If not, create it
     */
    private void handleResponse(FundamentalData fd, String symbol) throws Exception {

        notifyProgress(2, 3, "updating db: "+symbol);

        List<MarketIndex> indexes = marketIndexService.findBySymbol(fd.getSymbol());
        if(indexes.size()>1){
            throw new Exception("Multiple instances ("+indexes.size()+") of symbol "+fd.getSymbol()+" found in the database.");
        }

        MarketIndex index;
        String action;
        if(indexes.size()==0){  // does not exist in db
            index=new MarketIndex();
            index.setSymbol(fd.getSymbol());
            updateIcon(index);
            action="created";
        }else{  // index exists in db
            index = indexes.get(0);
            action="updated";
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

        log.info(symbol+" "+action);

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
            log.info("Download task aborted by user");
        }else{
            log.error("Download task error", e);
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
