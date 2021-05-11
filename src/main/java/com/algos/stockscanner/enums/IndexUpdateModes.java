package com.algos.stockscanner.enums;

public enum IndexUpdateModes {
    ALL_DATA("Download all available data"),
    MISSING_DATA("Download only missing data");

    private String description;

    IndexUpdateModes(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return description;
    }

}
