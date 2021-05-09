package com.algos.stockscanner.enums;

import java.util.Optional;

public enum FrequencyTypes {

    DAILY ("DAILY","Daily"),
    WEEKLY ("WEEKLY","Weekly"),
    MONTHLY("MONTHLY","Monthly"),
    INTRADAY1 ("INTRADAY1","Intraday 1 min"),
    INTRADAY5 ("INTRADAY5","Intraday 5 min"),
    INTRADAY60 ("INTRADAY60","Intraday 1 hr");

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

    FrequencyTypes(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static Optional<FrequencyTypes> getItem(String itemCode){
        FrequencyTypes[] elements = FrequencyTypes.values();
        for(FrequencyTypes element : elements){
            if(element.getCode().equals(itemCode)){
                return Optional.of(element);
            }
        }
        return Optional.empty();
    }

    public static Optional<String> getDescription(String itemCode) {
        Optional<FrequencyTypes> item = getItem(itemCode);
        if(item.isPresent()){
            return Optional.of(item.get().getDescription());
        }
        return Optional.empty();
    }
}
