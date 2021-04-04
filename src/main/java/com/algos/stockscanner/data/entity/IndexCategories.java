package com.algos.stockscanner.data.entity;

public enum IndexCategories {

    STOCKTIMESERIES ("STOCK","Stock Time Series Data"),
    FOREXRATE ("FOREX","Forex Rate Data"),
    EXCHANGERATE ("EXCHANGE","Exchange Rate Data"),
    DIGITALCURRENCY("CRYPTO","Digital Currency Data"),
    TECHNICALINDICATOR("TECH","Technical Indicator Data"),
    SECTORPERFORMANCE("SECTOR","Sector Performance Data");

    private String code;
    private String description;

    IndexCategories(String code, String description) {
        this.code = code;
        this.description = description;
    }

}
