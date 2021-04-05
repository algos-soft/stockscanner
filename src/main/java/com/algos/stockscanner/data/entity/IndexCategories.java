package com.algos.stockscanner.data.entity;

import java.util.Optional;

public enum IndexCategories {

    STOCKTIMESERIES ("STOCK","Stock"),
    FOREXRATE ("FOREX","Forex"),
    EXCHANGERATE ("EXCHANGE","Exchange Rate"),
    DIGITALCURRENCY("CRYPTO","Digital Currency"),
    TECHNICALINDICATOR("TECH","Technical Indicator"),
    SECTORPERFORMANCE("SECTOR","Sector Performance");

    private String code;
    private String description;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    IndexCategories(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static Optional<IndexCategories> getItem(String categoryCode){
        IndexCategories[] elements = IndexCategories.values();
        for(IndexCategories element : elements){
            if(element.getCode().equals(categoryCode)){
                return Optional.of(element);
            }
        }
        return Optional.empty();
    }

    public static Optional<String> getDescription(String categoryCode) {
        Optional<IndexCategories> category = getItem(categoryCode);
        if(category.isPresent()){
            return Optional.of(category.get().getDescription());
        }
        return Optional.empty();
    }
}
