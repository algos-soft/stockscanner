package com.algos.stockscanner.data.enums;

public enum Reasons {
    BELOW_THRESHOLD("BT"),
    ABOVE_THRESHOLD("AT");

    String code;

    Reasons(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
