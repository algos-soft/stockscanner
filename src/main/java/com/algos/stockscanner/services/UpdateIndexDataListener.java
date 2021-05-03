package com.algos.stockscanner.services;

import java.time.LocalDate;

public interface UpdateIndexDataListener {
    void onProgress(int current, int total, LocalDate date);
    void onCompleted(boolean aborted);
    void onError(Exception e);
}
