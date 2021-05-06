package com.algos.stockscanner.services;

import com.algos.stockscanner.beans.ContextStore;
import com.algos.stockscanner.data.entity.IndexUnit;
import com.algos.stockscanner.data.entity.MarketIndex;
import com.algos.stockscanner.data.enums.FrequencyTypes;
import com.algos.stockscanner.data.enums.IndexCategories;
import com.algos.stockscanner.data.service.IndexUnitService;
import com.algos.stockscanner.data.service.MarketIndexService;
import com.algos.stockscanner.task.TaskHandler;
import com.algos.stockscanner.task.TaskListener;
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

    private static final Logger log = LoggerFactory.getLogger(UpdateIndexDataCallable.class);

    private static final DateTimeFormatter fmt = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd")
            .optionalStart()
            .appendPattern(" HH:mm")
            .optionalEnd()
            .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
            .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
            .toFormatter();

    private MarketIndex index;
    private String mode;
    private LocalDate startDate;
    private ConcurrentLinkedQueue<TaskListener> listeners = new ConcurrentLinkedQueue<>();
    private ConcurrentLinkedQueue<TaskHandler> handlers = new ConcurrentLinkedQueue<>();
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @Autowired
    private ContextStore contextStore;

    @Autowired
    private IndexUnitService indexUnitService;

    @Autowired
    private MarketIndexService marketIndexService;

    /**
     * @param index     the index to update
     * @param mode      the update mode
     *                  ALL - delete all index data and load all the available data
     *                  DATE - add/update all data starting from the given date included
     * @param startDate in case of DATE mode, the date when to begin the update
     */
    public UpdateIndexDataCallable(MarketIndex index, String mode, LocalDate startDate) {
        this.index = index;
        this.mode = mode;
        this.startDate = startDate;
    }

    @PostConstruct
    private void init() {

        // register itself to the context-level storage
        contextStore.updateIndexCallableMap.put("" + hashCode(), this);

    }

    @Override
    public Void call() {

        log.debug("Callable task called for index " + index.getSymbol());

        startTime = LocalDateTime.now();

        // long task, can throw exception
        try {

            checkHandlers();    // throws exception if at least one handler is aborted

            // retrieve the category
            java.util.Optional<IndexCategories> oCategory = IndexCategories.getItem(index.getCategory());
            if (!oCategory.isPresent()) {
                throw new Exception("Index " + index.getSymbol() + " does not have a category.");
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

                    TimeSeriesResponse response=AlphaVantage.api()
                            .timeSeries()
                            .daily()
                            .forSymbol(index.getSymbol())
                            .outputSize(OutputSize.FULL)
                            .dataType(DataType.JSON)
                            .fetchSync();

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
            log.debug("Callable task completed for index " + index.getSymbol() + " " + info);
            notifyCompleted(info);

        } catch (Exception e) {

            endTime = LocalDateTime.now();
            log.error("Callable task error for index " + index.getSymbol(), e);
            notifyError(e);

        }

        // unregister itself from the context-level storage
        contextStore.updateIndexCallableMap.remove("" + hashCode(), this);

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
    }

    /**
     * @param handler handler to check for external abort requests (optional)
     */
    public void addHandler(TaskHandler handler) {
        this.handlers.add(handler);
    }

    private void notifyProgress(int current, int tot, String info) {
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

    private void checkHandlers() throws Exception {
        for (TaskHandler handler : handlers) {
            if (handler.isAborted()) {
                throw new Exception("Aborted by user");
            }
        }
    }


    /**
     * Manage a successful response from the api
     */
    public void handleResponse(TimeSeriesResponse response)  throws Exception {

        // delete all previous unit data
        notifyProgress(0, 0, "Deleting old data");
        indexUnitService.deleteByIndex(index);

        // Iterate the new units and save them
        List<StockUnit> units = response.getStockUnits();
        Collections.sort(units, Comparator.comparing(StockUnit::getDate));
        int j = 0;
        LocalDateTime minDateTime = null;
        LocalDateTime maxDateTime = null;
        for (StockUnit unit : units) {

            checkHandlers();

            j++;

            notifyProgress(j, units.size(), index.getSymbol());

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


}
