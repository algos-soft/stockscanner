package com.algos.stockscanner.task;

public interface TaskListener {
    void onProgress(int current, int total, Object info);
    void onCompleted(boolean aborted);
    void onError(Exception e);
}
