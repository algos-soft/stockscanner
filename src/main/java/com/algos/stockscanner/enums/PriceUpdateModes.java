package com.algos.stockscanner.enums;

public enum PriceUpdateModes {
    REPLACE_ALL_DATA("Download all available prices"),
    ADD_MISSING_DATA_ONLY("Download only missing prices");

    private String description;

    PriceUpdateModes(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return description;
    }

}
