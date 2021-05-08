package com.algos.stockscanner.services;

public class IndexEntry {

    public String symbol;

    public String type;

    public IndexEntry(String symbol, String type) {
        this.symbol = symbol;
        this.type = type;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return "IndexEntry{" +
                "symbol='" + symbol + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
