package com.algos.stockscanner.services;

import com.algos.stockscanner.data.entity.IndexUnit;
import com.squareup.moshi.Json;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Maps the response of the AlphaValtage API TIME_SERIES_DAILY_ADJUSTED
 */
public class TimeSeriesDailyAdjusted {

    @Json(name = "Meta Data")
    MetaData metaData;

    @Json(name = "Time Series (Daily)")
    Map<String, TimeSeriesUnit> timeSeriesUnitMap;


    public static class MetaData{
        @Json(name = "1. Information")
        String information;
        @Json(name = "2. Symbol")
        String symbol;
        @Json(name = "3. Last Refreshed")
        String lastRefreshed;
        @Json(name = "4. Output Size")
        String outputSize;
        @Json(name = "5. Time Zone")
        String timeZone;
    }

    public static class TimeSeriesUnit{
        @Json(name = "1. open")
        String open;
        @Json(name = "2. high")
        String high;
        @Json(name = "3. low")
        String low;
        @Json(name = "4. close")
        String close;
        @Json(name = "5. adjusted close")
        String adjustedClose;
        @Json(name = "6. volume")
        String volume;
        @Json(name = "7. dividend amount")
        String dividendAmount;
        @Json(name = "8. split coefficient")
        String splitCoefficient;

    }


    public List<IndexUnit> getIndexUnits() {
        List<IndexUnit> indexUnits = new ArrayList<>();
        Set<Map.Entry<String, TimeSeriesUnit>> entries = timeSeriesUnitMap.entrySet();
        for(Map.Entry<String, TimeSeriesUnit> entry : entries){
            String key = entry.getKey();
            TimeSeriesUnit value = entry.getValue();

            IndexUnit indexUnit=new IndexUnit();
            indexUnit.setOpen(Float.parseFloat(value.open));
            indexUnit.setClose(Float.parseFloat(value.adjustedClose));
            LocalDate ld = LocalDate.parse(key);
            indexUnit.setDateTimeLDT(ld.atStartOfDay());

            indexUnits.add(indexUnit);

        }
        return indexUnits;
    }



}
