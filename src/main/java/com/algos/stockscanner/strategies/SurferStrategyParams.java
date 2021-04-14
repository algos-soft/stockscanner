package com.algos.stockscanner.strategies;

import com.algos.stockscanner.data.entity.MarketIndex;

public class SurferStrategyParams implements StrategyParams {
    private MarketIndex index;

    public MarketIndex getIndex() {
        return index;
    }

    public void setIndex(MarketIndex index) {
        this.index = index;
    }

}
