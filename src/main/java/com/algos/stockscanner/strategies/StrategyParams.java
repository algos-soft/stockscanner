package com.algos.stockscanner.strategies;

import com.algos.stockscanner.data.entity.MarketIndex;

import java.time.LocalDate;

public interface StrategyParams {
    public MarketIndex getIndex();
    public LocalDate getStartDate();
}
