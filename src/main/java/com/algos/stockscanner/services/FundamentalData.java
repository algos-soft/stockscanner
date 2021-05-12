package com.algos.stockscanner.services;

import com.algos.stockscanner.enums.IndexCategories;

class FundamentalData {
    private String symbol;
    private String name;
    private IndexCategories type;

    private String exchange;
    private String country;
    private String sector;
    private String industry;
    private long marketCap;
    private long ebitda;

    public FundamentalData(String symbol, String name, IndexCategories type, String exchange, String country, String sector, String industry, long marketCap, long ebitda) {
        this.symbol = symbol;
        this.name = name;
        this.type = type;

        this.exchange=exchange;
        this.country=country;
        this.sector=sector;
        this.industry=industry;
        this.marketCap=marketCap;
        this.ebitda=ebitda;
    }

//    public FundamentalData(String symbol, String name, IndexCategories type) {
//        this(symbol, name, type, null, null, null, null, 0, 0);
//    }


    public String getSymbol() {
        return symbol;
    }

    public String getName() {
        return name;
    }

    public IndexCategories getType() {
        return type;
    }

    public String getExchange() {
        return exchange;
    }

    public String getCountry() {
        return country;
    }

    public String getSector() {
        return sector;
    }

    public String getIndustry() {
        return industry;
    }

    public long getMarketCap() {
        return marketCap;
    }

    public long getEbitda() {
        return ebitda;
    }
}
