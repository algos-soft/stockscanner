package com.algos.stockscanner.strategies;

import com.algos.stockscanner.data.entity.Simulation;
import com.algos.stockscanner.data.entity.SimulationItem;
import com.algos.stockscanner.data.enums.Terminations;

public interface Strategy {
    String getCode();

    Simulation execute() throws Exception;

    void abort();

    SimulationItem processUnit() throws Exception;

    Terminations isFinished();

    Decision takeDecision();

    Decision decideIfCloseBuyPosition();

    Decision decideIfCloseSellPosition();

    Decision decideIfOpenPosition();
}
