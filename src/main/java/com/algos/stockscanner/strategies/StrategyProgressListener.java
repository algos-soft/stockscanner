package com.algos.stockscanner.strategies;

public interface StrategyProgressListener {
    void notifyProgress(int current, int total, String status);
}
