package com.algos.stockscanner.enums;

public enum Terminations {
    NO_MORE_DATA ("NO_MORE_DATA"),
    MAX_DAYS_REACHED ("MAX_DAYS_REACHED"),
    ABORTED_BY_USER ("ABORTED_BY_USER"),
    STOP_LOSS_REACHED ("STOP_LOSS_REACHED"),
    TAKE_PROFIT_REACHED ("TAKE_PROFIT_REACHED");

    //WARNING: IF YOU ADD ITEMS HERE, ADD THE SAME ITEMS IN Reasons ENUM!

    String code;  // reason code for the db

    Terminations(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
