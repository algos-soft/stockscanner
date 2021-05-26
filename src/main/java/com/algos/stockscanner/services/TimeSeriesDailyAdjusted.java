package com.algos.stockscanner.services;

import com.algos.stockscanner.data.entity.IndexUnit;

import java.util.List;

public class TimeSeriesDailyAdjusted {


    private String symbol;

    private List<IndexUnit> units;

    public TimeSeriesDailyAdjusted(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public List<IndexUnit> getUnits() {
        return units;
    }

    public void setUnits(List<IndexUnit> units) {
        this.units = units;
    }
}
