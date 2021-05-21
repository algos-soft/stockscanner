package com.algos.stockscanner.enums;

public enum PriceUpdateModes {
    ADD_MISSING_DATA_ONLY("Update prices"),
    REPLACE_ALL_DATA("Reload prices");

    private String description;

    PriceUpdateModes(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return description;
    }

}
