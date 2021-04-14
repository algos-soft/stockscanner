package com.algos.stockscanner.strategies;

import com.algos.stockscanner.data.entity.MarketIndex;

import java.time.LocalDate;

public class SurferStrategyParams implements StrategyParams {
    private MarketIndex index;
    private LocalDate startDate;

    public MarketIndex getIndex() {
        return index;
    }

    public void setIndex(MarketIndex index) {
        this.index = index;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }
}
