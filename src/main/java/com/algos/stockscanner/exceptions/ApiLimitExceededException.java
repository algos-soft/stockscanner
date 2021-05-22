package com.algos.stockscanner.exceptions;

public class ApiLimitExceededException extends Exception {
    public ApiLimitExceededException() {
        super("Api limit exceeded");
    }
}

