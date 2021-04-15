package com.algos.stockscanner.data.enums;

public enum Terminations {
    NO_MORE_DATA ("NO_MORE_DATA"),
    MAX_DAYS_REACHED ("MAX_DAYS_REACHED"),
    ABORTED_BY_USER ("ABORTED_BY_USER");

    String code;  // reason code for the db

    Terminations(String code) {
        this.code = code;
    }
}
