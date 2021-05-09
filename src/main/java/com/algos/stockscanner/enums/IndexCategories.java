package com.algos.stockscanner.enums;

import java.util.Optional;

public enum IndexCategories {

    STOCK("STOCK","Stock", "Common Stock"),
    FOREX("FOREX","Forex", null),
    EXCHANGE("EXCHANGE","Exchange Rate", null),
    CRYPTO("CRYPTO","Digital Currency", null),
    TECH("TECH","Technical Indicator", null),
    SECTOR("SECTOR","Sector Performance", null);

    private String code;
    private String description;
    private String alphaVantageType;    // how is returned by AlphaVantage API in Fundamentals Info

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }


    public String getAlphaVantageType() {
        return alphaVantageType;
    }

    IndexCategories(String code, String description, String alphaVantageType) {
        this.code = code;
        this.description = description;
        this.alphaVantageType = alphaVantageType;
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

    public static IndexCategories getByAlphaVantageType(String type){
        for(IndexCategories cat : values()){
            if(cat!=null){
                if(cat.getAlphaVantageType()!=null){
                    if(cat.getAlphaVantageType().equals(type)){
                        return cat;
                    }
                }
            }
        }
        return null;
    }
}
