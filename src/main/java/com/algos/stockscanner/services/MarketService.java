package com.algos.stockscanner.services;

import com.algos.stockscanner.beans.HttpClient;
import com.algos.stockscanner.data.entity.IndexCategories;
import com.algos.stockscanner.data.entity.IndexUnit;
import com.algos.stockscanner.data.entity.MarketIndex;
import com.algos.stockscanner.data.service.IndexUnitRepository;
import com.algos.stockscanner.data.service.IndexUnitService;
import com.algos.stockscanner.data.service.MarketIndexRepository;
import com.algos.stockscanner.data.service.MarketIndexService;
import com.crazzyghost.alphavantage.AlphaVantage;
import com.crazzyghost.alphavantage.AlphaVantageException;
import com.crazzyghost.alphavantage.parameters.DataType;
import com.crazzyghost.alphavantage.parameters.OutputSize;
import com.crazzyghost.alphavantage.timeseries.response.StockUnit;
import com.crazzyghost.alphavantage.timeseries.response.TimeSeriesResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Iterator;
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
    private MarketIndexRepository marketIndexRepository;

    @Autowired
    private MarketIndexService marketIndexService;

    @Autowired
    private IndexUnitRepository indexUnitRepository;

    @Autowired
    private IndexUnitService indexUnitService;


    @Autowired
    private HttpClient httpClient;

    @Value("${alphavantage.api.key}")
    private String alphavantageApiKey;


    /**
     * Download market data for the given symbol and store it in the db
     */
    public DownloadHandler download(String symbol, DownloadListener downloadListener) {

        DownloadHandler downloadHandler=null;

        // retrieve the item from the db
        MarketIndex item;
        MarketIndex miExample = new MarketIndex();
        miExample.setSymbol(symbol);
        List<MarketIndex> list = marketIndexRepository.findAll(Example.of(miExample));
        if(list.size()!=1){
            downloadListener.onDownloadAborted(new Exception("Symbol "+symbol+" not found in database."));
            return null;
        }
        item=list.get(0);

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
        List<IndexUnit> indexUnits = indexUnitRepository.findByIndex(marketIndex);
        int i=0;
        int tot=indexUnits.size();
        for(IndexUnit iu : indexUnits){
            i++;
            downloadListener.onDownloadProgress(i,tot,"Deleting old data");
            indexUnitService.delete(iu.getId());
        }

        // this approach must be done in batches of 100 records or you get StackOverflow
        // Iterable<IndexUnit> iterable = () -> indexUnits.iterator();
        // indexUnitRepository.deleteInBatch(iterable);

        // Iterate the new units and save them
        List<StockUnit> units = response.getStockUnits();
        int j=0;
        for(StockUnit unit : units){
            if (downloadHandler.isAbort()){
                downloadListener.onDownloadAborted(new Exception("Abort requested by client"));
                return;
            }
            j++;
            downloadListener.onDownloadProgress(j, units.size(),"Processing item");
            saveItem(unit, marketIndex);
        }

        // completed
        downloadListener.onDownloadCompleted();

    }

    public void handleFailure(AlphaVantageException error, DownloadListener downloadListener) {
        downloadListener.onDownloadAborted(error);
    }


    private void saveItem(StockUnit unit, MarketIndex marketIndex){
        IndexUnit indexUnit = createIndexUnit(unit);
        indexUnit.setIndex(marketIndex);
        indexUnitRepository.save(indexUnit);
    }

    private IndexUnit createIndexUnit(StockUnit unit){
        IndexUnit indexUnit = new IndexUnit();
        indexUnit.setOpen((float)unit.getOpen());
        indexUnit.setClose((float)unit.getClose());

        LocalDate date = LocalDate.parse(unit.getDate(), fmt);

        LocalDateTime dateTime = LocalDateTime.parse(unit.getDate(), fmt);

        indexUnit.setDateTime(dateTime);

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
