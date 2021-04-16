package com.algos.stockscanner.data.enums;

public enum Actions {
    BUY ("BUY"),
    SELL ("SELL"),
    STAY("STAY");

    String code;

    Actions(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
