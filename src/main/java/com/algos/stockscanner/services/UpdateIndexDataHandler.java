package com.algos.stockscanner.services;

public class UpdateIndexDataHandler {

    private boolean aborted;

    public void abort(){
        aborted =true;
    }

    public boolean isAborted() {
        return aborted;
    }
}
