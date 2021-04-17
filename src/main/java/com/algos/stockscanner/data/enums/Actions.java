package com.algos.stockscanner.data.enums;

public enum Actions {
    OPEN ("OPEN"),
    CLOSE ("CLOSE"),
    STAY("STAY");

    String code;

    Actions(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
