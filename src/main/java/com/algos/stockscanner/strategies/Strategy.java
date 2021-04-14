package com.algos.stockscanner.strategies;

public interface Strategy {
    String getCode();

    void execute() throws Exception;
    void abort();
    boolean isFinished();

}
