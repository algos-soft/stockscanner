package com.algos.stockscanner.services;

import com.algos.stockscanner.beans.HttpClient;
import com.algos.stockscanner.beans.Utils;
import com.algos.stockscanner.data.enums.FrequencyTypes;
import com.algos.stockscanner.data.enums.IndexCategories;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
public class MarketService {

    private  static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private  static final DateTimeFormatter fmt = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd")
            .optionalStart()
            .appendPattern(" HH:mm")
            .optionalEnd()
            .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
            .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
            .toFormatter();


    //private final Moshi moshi = new Moshi.Builder().build();


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


    /**
     * Download market data for the given symbol and store it in the db
     */
    public DownloadHandler download(String symbol, DownloadListener downloadListener) {

        DownloadHandler downloadHandler=null;

        MarketIndex item;
        try {
            item = marketIndexService.findUniqueBySymbol(symbol);
        } catch (Exception e) {
            downloadListener.onDownloadAborted(e);
            return null;
        }

        // retrieve the category
        java.util.Optional<IndexCategories> oCategory = IndexCategories.getItem(item.getCategory());
        if(!oCategory.isPresent()){
            downloadListener.onDownloadAborted(new Exception("Index "+symbol+" does not have a category."));
            return null;
        }
        IndexCategories category=oCategory.get();

        // switch on the category
        String url=null;
        switch (category){
            case DIGITALCURRENCY:
                break;
            case EXCHANGERATE:
                break;
            case FOREXRATE:
                break;
            case SECTORPERFORMANCE:
                break;
            case STOCKTIMESERIES:

                downloadHandler=new DownloadHandler();
                DownloadHandler finalDownloadHandler = downloadHandler;

                downloadListener.onDownloadProgress(0, 0, "Requesting data");

                AlphaVantage.api()
                        .timeSeries()
                        .daily()
                        .forSymbol(symbol)
                        .outputSize(OutputSize.FULL)
                        .dataType(DataType.JSON)
                        .onSuccess(e->handleSuccess((TimeSeriesResponse)e, item, downloadListener, finalDownloadHandler))
                        .onFailure(e->handleFailure(e, downloadListener))
                        .fetch();

                break;
            case TECHNICALINDICATOR:
                break;
        }

        return downloadHandler;

    }

    public void handleSuccess(TimeSeriesResponse response, MarketIndex marketIndex, DownloadListener downloadListener, DownloadHandler downloadHandler) {

        // delete all previous unit data
        downloadListener.onDownloadProgress(0,0,"Deleting old data");
        indexUnitService.deleteByIndex(marketIndex);

        // Iterate the new units and save them
        List<StockUnit> units = response.getStockUnits();
        Collections.sort(units, Comparator.comparing(StockUnit::getDate));
        int j=0;
        LocalDateTime minDateTime=null;
        LocalDateTime maxDateTime=null;
        for(StockUnit unit : units){
            if (downloadHandler.isAbort()){
                downloadListener.onDownloadAborted(new Exception("Abort requested by client"));
                return;
            }
            j++;
            downloadListener.onDownloadProgress(j, units.size(),"Processing item");
            IndexUnit indexUnit = saveItem(unit, marketIndex);

            // keep minDateTime and maxDateTime up to date
            if(minDateTime==null){
                minDateTime=indexUnit.getDateTimeLDT();
            }else{
                if(indexUnit.getDateTimeLDT().isBefore(minDateTime)){
                    minDateTime=indexUnit.getDateTimeLDT();
                }
            }
            if(maxDateTime==null){
                maxDateTime=indexUnit.getDateTimeLDT();
            }else{
                if(indexUnit.getDateTimeLDT().isAfter(maxDateTime)){
                    maxDateTime=indexUnit.getDateTimeLDT();
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


    private IndexUnit saveItem(StockUnit unit, MarketIndex marketIndex){
        IndexUnit indexUnit = createIndexUnit(unit);
        indexUnit.setIndex(marketIndex);
        return indexUnitService.update(indexUnit);
    }

    private IndexUnit createIndexUnit(StockUnit unit){
        IndexUnit indexUnit = new IndexUnit();
        indexUnit.setOpen((float)unit.getOpen());
        indexUnit.setClose((float)unit.getClose());

        //LocalDate date = LocalDate.parse(unit.getDate(), fmt);

        LocalDateTime dateTime = LocalDateTime.parse(unit.getDate(), fmt);

        indexUnit.setDateTimeLDT(dateTime);

        return indexUnit;
    }


    public interface DownloadListener {
        void onDownloadCompleted();
        void onDownloadAborted(Exception e);
        void onDownloadProgress(int current, int total, String message);
    };

    public class DownloadHandler{
        boolean abort=false;

        public boolean isAbort() {
            return abort;
        }

        public void setAbort(boolean abort) {
            this.abort = abort;
        }
    };


}
