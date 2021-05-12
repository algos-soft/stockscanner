package com.algos.stockscanner.strategies;

import com.algos.stockscanner.data.entity.MarketIndex;
import com.algos.stockscanner.data.entity.Simulation;
import com.algos.stockscanner.data.entity.SimulationItem;
import com.algos.stockscanner.enums.Terminations;

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

    void addProgressListener(StrategyProgressListener listener);

}
