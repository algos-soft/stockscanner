package com.algos.stockscanner.strategies;

import com.algos.stockscanner.data.entity.IndexUnit;
import com.algos.stockscanner.data.entity.Simulation;
import com.algos.stockscanner.data.enums.Actions;
import com.algos.stockscanner.data.enums.Terminations;

public interface Strategy {
    String getCode();

    Simulation execute() throws Exception;

    void abort();

    void processUnit() throws Exception;

    ActionReason takeDecision();

    Terminations isFinished();

}
