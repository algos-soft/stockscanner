package com.algos.stockscanner.strategies;

import com.algos.stockscanner.data.entity.IndexUnit;
import com.algos.stockscanner.data.enums.Terminations;

public interface Strategy {
    String getCode();

    void execute() throws Exception;
    void abort();
    void processUnit() throws Exception;
    Terminations isFinished();


}
