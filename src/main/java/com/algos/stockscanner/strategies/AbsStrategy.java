package com.algos.stockscanner.strategies;

import com.algos.stockscanner.data.service.IndexUnitService;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbsStrategy implements Strategy {

    StrategyParams params;
    boolean abort=false;

    @Autowired
    IndexUnitService indexUnitService;

    public AbsStrategy(StrategyParams params) {
        this.params=params;
    }

//    @Override
//    public void execute() throws Exception {
//        abort=false;
//    }

    @Override
    public void abort() {
        abort=true;
    }

}
