package com.algos.stockscanner.strategies;

import com.algos.stockscanner.data.entity.IndexUnit;

public interface Strategy {
    String getCode();

    void execute() throws Exception;
    void abort();
    void processUnit() throws Exception;
    boolean isFinished();


}
