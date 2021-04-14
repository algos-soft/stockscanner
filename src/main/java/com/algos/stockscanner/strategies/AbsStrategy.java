package com.algos.stockscanner.strategies;

public abstract class AbsStrategy implements Strategy {

    StrategyParams params;
    boolean abort=false;

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
