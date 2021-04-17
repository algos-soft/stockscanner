package com.algos.stockscanner.data.enums;

import java.util.Arrays;

public enum Actions {
    OPEN ("OPEN"),
    CLOSE ("CLOSE"),
    STAY("STAY");

    String code;

    Actions(String code) {
        this.code = code;
    }

    public static Actions get(String code) {
        for(Actions a : values()){
            if(a.getCode().equals(code)){
                return a;
            }
        }
        return null;
    }

    public String getCode() {
        return code;
    }
}
