package com.algos.stockscanner.task;

public class AbortedByUserException extends Exception {
    public AbortedByUserException() {
        super("Aborted by user");
    }
}
