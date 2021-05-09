package com.algos.stockscanner.data.enums;

public enum IndexDownloadModes {
    NEW("Download new indexes"),
    UPDATE("Update existing indexes"),
    NEW_AND_UPDATE("Download new and update existing");

    private String description;

    IndexDownloadModes(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return description;
    }

}
