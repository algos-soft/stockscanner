package com.algos.stockscanner.services;

import com.algos.stockscanner.Application;
import com.algos.stockscanner.beans.HttpClient;
import com.algos.stockscanner.beans.Utils;
import com.algos.stockscanner.enums.FrequencyTypes;
import com.algos.stockscanner.enums.IndexCategories;
import com.algos.stockscanner.data.entity.IndexUnit;
import com.algos.stockscanner.data.entity.MarketIndex;
import com.algos.stockscanner.data.service.IndexUnitService;
import com.algos.stockscanner.data.service.MarketIndexService;
import com.crazzyghost.alphavantage.AlphaVantage;
import com.crazzyghost.alphavantage.AlphaVantageException;
import com.crazzyghost.alphavantage.parameters.DataType;
import com.crazzyghost.alphavantage.parameters.OutputSize;
import com.crazzyghost.alphavantage.timeseries.response.StockUnit;
import com.crazzyghost.alphavantage.timeseries.response.TimeSeriesResponse;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.vaadin.flow.component.html.Image;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.*;

@Service
public class MarketService {

    private static final Logger log = LoggerFactory.getLogger(MarketService.class);

    @Autowired
    ResourceLoader resourceLoader;

    private static final DateTimeFormatter fmt = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd")
            .optionalStart()
            .appendPattern(" HH:mm")
            .optionalEnd()
            .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
            .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
            .toFormatter();

    private final Moshi moshi = new Moshi.Builder().build();
    private final JsonAdapter<FDResponse> fdJsonAdapter = moshi.adapter(FDResponse.class);


    @Autowired
    private ApplicationContext context;

    @Autowired
    private MarketIndexService marketIndexService;

    @Autowired
    private IndexUnitService indexUnitService;


    @Autowired
    private HttpClient httpClient;

    @Autowired
    private Utils utils;

    @Value("${alphavantage.api.key}")
    private String alphavantageApiKey;

    private OkHttpClient okHttpClient;

    private HashSet<String> eToroInstruments = new HashSet<String>();


    @PostConstruct
    private void init(){

        okHttpClient = new OkHttpClient();

        loadEtoroInstruments();

    }




    private void loadEtoroInstruments(){

        String filename="config/etoro_instruments.csv";
        File etoroInstrumentsFile = new File(filename);
        if(!etoroInstrumentsFile.exists()){
            log.warn("File "+filename+" not found. Can't load list of eToro instruments.");
            return;
        }

        try {
            List<String> lines = Files.readAllLines(etoroInstrumentsFile.toPath());
            for(String line : lines){
                eToroInstruments.add(line);
            }
        } catch (IOException e) {
            log.error("could not read eToro instruments file "+etoroInstrumentsFile.toString(), e);
        }
    }



    /**
     * Download market data for the given symbol and store it in the db
     */
    public DownloadHandler downloadIndexData(String symbol, DownloadListener downloadListener) {

        DownloadHandler downloadHandler = null;

        MarketIndex item;
        try {
            item = marketIndexService.findUniqueBySymbol(symbol);
        } catch (Exception e) {
            downloadListener.onDownloadAborted(e);
            return null;
        }

        // retrieve the category
        java.util.Optional<IndexCategories> oCategory = IndexCategories.getItem(item.getCategory());
        if (!oCategory.isPresent()) {
            downloadListener.onDownloadAborted(new Exception("Index " + symbol + " does not have a category."));
            return null;
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

                downloadHandler = new DownloadHandler();
                DownloadHandler finalDownloadHandler = downloadHandler;

                downloadListener.onDownloadProgress(0, 0, "Requesting data");

                AlphaVantage.api()
                        .timeSeries()
                        .daily()
                        .forSymbol(symbol)
                        .outputSize(OutputSize.FULL)
                        .dataType(DataType.JSON)
                        .onSuccess(e -> indexDataHandleSuccess((TimeSeriesResponse) e, item, downloadListener, finalDownloadHandler))
                        .onFailure(e -> handleFailure(e, downloadListener))
                        .fetch();

                break;
            case TECH:
                break;
        }

        return downloadHandler;

    }

    public void indexDataHandleSuccess(TimeSeriesResponse response, MarketIndex marketIndex, DownloadListener downloadListener, DownloadHandler downloadHandler) {

        // delete all previous unit data
        downloadListener.onDownloadProgress(0, 0, "Deleting old data");
        indexUnitService.deleteByIndex(marketIndex);

        // Iterate the new units and save them
        List<StockUnit> units = response.getStockUnits();
        Collections.sort(units, Comparator.comparing(StockUnit::getDate));
        int j = 0;
        LocalDateTime minDateTime = null;
        LocalDateTime maxDateTime = null;
        for (StockUnit unit : units) {
            if (downloadHandler.isAbort()) {
                downloadListener.onDownloadAborted(new Exception("Abort requested by client"));
                return;
            }
            j++;
            downloadListener.onDownloadProgress(j, units.size(), "Processing item");
            IndexUnit indexUnit = saveItem(unit, marketIndex);

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
        marketIndex.setUnitsFromLD(minDateTime.toLocalDate());
        marketIndex.setUnitsToLD(maxDateTime.toLocalDate());
        marketIndex.setNumUnits(units.size());
        marketIndex.setUnitFrequency(FrequencyTypes.DAILY.getCode());
        marketIndexService.update(marketIndex);


        // completed
        downloadListener.onDownloadCompleted();

    }

    public void handleFailure(AlphaVantageException error, DownloadListener downloadListener) {
        downloadListener.onDownloadAborted(error);
    }


    private IndexUnit saveItem(StockUnit unit, MarketIndex marketIndex) {
        IndexUnit indexUnit = createIndexUnit(unit);
        indexUnit.setIndex(marketIndex);
        return indexUnitService.update(indexUnit);
    }

    private IndexUnit createIndexUnit(StockUnit unit) {
        IndexUnit indexUnit = new IndexUnit();
        indexUnit.setOpen((float) unit.getOpen());
        indexUnit.setClose((float) unit.getClose());

        //LocalDate date = LocalDate.parse(unit.getDate(), fmt);

        LocalDateTime dateTime = LocalDateTime.parse(unit.getDate(), fmt);

        indexUnit.setDateTimeLDT(dateTime);

        return indexUnit;
    }


    /**
     * Download all the indexes declared in the resource file indexes.csv
     * @param maxReqPerMinute max request per minute to the API, 0=unlimited
     */
    public DownloadHandler downloadIndexes(DownloadListener downloadListener, int maxReqPerMinute) {
        DownloadHandler downloadHandler = null;
        downloadHandler = new DownloadHandler();

        // retrieve the list of indexes to download from resources
        String filename="config/indexes.csv";
        File indexesFile = new File(filename);
        if(!indexesFile.exists()){
            log.error("File "+filename+" not found. Can't download indexes.");
            return null;
        }

        // parse the file into a list of objects
        List<IndexEntry> entries = new ArrayList<>();
        try {
            FileReader fileReader = new FileReader(indexesFile);
            CSVReader reader = new CSVReader(fileReader);
            List<String[]> list = reader.readAll();
            for(String[] element : list){
                if(element.length>=2){
                    String symbol = element[0].trim();
                    String type = element[1].trim();
                    entries.add(new IndexEntry(symbol, type));
                }else{
                    throw new IOException("Invalid element "+element+" in "+filename);
                }
            }
        } catch (IOException | CsvException e ) {
            downloadListener.onDownloadAborted(new Exception("Malformed resource file "+filename));
            log.error("could not parse the file "+indexesFile.toString(), e);
        }

        int sleepMillis=0;
        if(maxReqPerMinute>0){
            sleepMillis=(int)(60f/(float)maxReqPerMinute*1000f);
        }

        // perform and process the requests to retrieve each symbol
        int i=0;
        for(IndexEntry entry : entries){
            i++;

            if(eToroInstruments.contains(entry.getSymbol())){

                IndexCategories category = IndexCategories.getItem(entry.getType()).get();

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

                        downloadListener.onDownloadProgress(i, entries.size(), entry.getSymbol());

                        FundamentalData fd;
                        try {
                            fd = fetchFundamentalData(entry.getSymbol());
                            syncIndex(fd);
                        } catch (IOException e) {
                            log.error("Fetch error - "+entry.getSymbol()+" skipped", e);
                        } catch (Exception e) {
                            log.error("Sync error - "+entry.getSymbol()+" skipped", e);
                        }

                        break;
                    case TECH:
                        break;
                }

                // going too fast can exceed API license limits
                if(sleepMillis>0){
                    try {
                        Thread.sleep(sleepMillis);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }else{
                log.warn(entry.getSymbol()+" skipped because not present on eToro");
            }

        }

        downloadListener.onDownloadCompleted();

        return downloadHandler;
    }




    /**
     * Find an index in the database.
     * If exists, update it
     * If not, create it
     */
    private void syncIndex(FundamentalData fd) throws Exception {

        List<MarketIndex> indexes = marketIndexService.findBySymbol(fd.getSymbol());
        if(indexes.size()>1){
            throw new Exception("Multiple instances ("+indexes.size()+") of symbol "+fd.getSymbol()+" present in the database.");
        }

        MarketIndex index;
        if(indexes.size()==0){  // does not exist in db
            index=new MarketIndex();
            index.setSymbol(fd.getSymbol());
            index.setName(fd.getName());
            index.setCategory(fd.getType().getCode());

        }else{  // index exists in db
            index = indexes.get(0);
            if(fd.getName()!=null){
                index.setName(fd.getName());
            }
            if(fd.getType()!=null){
                index.setCategory(fd.getType().getCode());
            }

        }

        updateIcon(index);
        marketIndexService.update(index);

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
                Image img = utils.getDefaultIndexIcon();
                bytes = utils.imageToByteArray(img);
                byte[] scaled = utils.scaleImage(bytes, Application.STORED_ICON_WIDTH, Application.STORED_ICON_HEIGHT);
                index.setImage(scaled);
            }
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

            FDResponse fdresp = fdJsonAdapter.fromJson(response.body().string());

            if(fdresp.Symbol==null){
                throw new IOException("malformed response, license limits reached?");
            }

            IndexCategories category = IndexCategories.getByAlphaVantageType(fdresp.AssetType);
            fundamentalData=new FundamentalData(symbol, fdresp.Name, category);

        }

        return fundamentalData;
    }



    public interface DownloadListener {
        void onDownloadCompleted();

        void onDownloadAborted(Exception e);

        void onDownloadProgress(int current, int total, String message);
    }


    public class DownloadHandler {
        boolean abort = false;

        public boolean isAbort() {
            return abort;
        }

        public void setAbort(boolean abort) {
            this.abort = abort;
        }
    }


    static class FDResponse {
        String Symbol;
        String Name;
        String AssetType;
    }




}
