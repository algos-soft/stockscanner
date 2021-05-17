package com.algos.stockscanner.services;

public class ApiLimitExceededException extends Exception {
    public ApiLimitExceededException() {
        super("Api limit exceeded");
    }
}

