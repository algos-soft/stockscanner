package com.algos.stockscanner.enums;

public enum IndexUpdateModes {
    REPLACE_ALL_DATA("Download all available data"),
    ADD_MISSING_DATA_ONLY("Download only missing data");

    private String description;

    IndexUpdateModes(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return description;
    }

}
