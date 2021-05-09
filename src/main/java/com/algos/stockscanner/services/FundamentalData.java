package com.algos.stockscanner.services;

import com.algos.stockscanner.enums.IndexCategories;

class FundamentalData {
    private String symbol;
    private String name;
    private IndexCategories type;

    public FundamentalData(String symbol, String name, IndexCategories type) {
        this.symbol = symbol;
        this.name = name;
        this.type = type;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getName() {
        return name;
    }

    public IndexCategories getType() {
        return type;
    }
}
