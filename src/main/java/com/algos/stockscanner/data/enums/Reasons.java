package com.algos.stockscanner.data.enums;

public enum Reasons {
    BELOW_THRESHOLD("BELOW_THRESHOLD"),
    ABOVE_THRESHOLD("ABOVE_THRESHOLD"),
    STILL_GOING_UP("STILL_GOING_UP"),
    STILL_GOING_DOWN("STILL_GOING_DOWN"),
    PRE_ALERT_SELL("PRE_ALERT_SELL"),
    PRE_ALERT_BUY("PRE_ALERT_BUY"),
    IN_BOUNDS("IN_BOUNDS"),

    ABORTED_BY_USER("IN_BOUNDS"),
    MAX_DAYS_REACHED("MAX_DAYS_REACHED"),
    NO_MORE_DATA("NO_MORE_DATA"),

    UNKNOWN("UNKNOWN");

    String code;

    Reasons(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static Reasons get(String code) {
        for(Reasons a : values()){
            if(a.getCode().equals(code)){
                return a;
            }
        }
        return null;
    }

}
