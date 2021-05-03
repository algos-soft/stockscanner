package com.algos.stockscanner.task;

public class TaskHandler {

    private boolean aborted;

    public void abort(){
        aborted =true;
    }

    public boolean isAborted() {
        return aborted;
    }
}
