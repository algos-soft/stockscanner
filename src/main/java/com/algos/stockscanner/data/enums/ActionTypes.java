package com.algos.stockscanner.data.enums;

public enum ActionTypes {
    BUY ("BUY"),
    SELL ("SELL");

    String code;

    ActionTypes(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static ActionTypes get(String code) {
        for(ActionTypes a : values()){
            if(a.getCode().equals(code)){
                return a;
            }
        }
        return null;
    }

}
