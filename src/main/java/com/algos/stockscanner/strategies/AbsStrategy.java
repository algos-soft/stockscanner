package com.algos.stockscanner.strategies;

import com.algos.stockscanner.data.entity.IndexUnit;
import com.algos.stockscanner.data.service.IndexUnitService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

public abstract class AbsStrategy implements Strategy {

    StrategyParams params;
    boolean abort=false;

    // current page of units scanned
    List<IndexUnit> unitsPage=new ArrayList<>();

    // Index of the current unit in the units page, 0 for the first
    int unitIndex=0;

    // Id of the currently scanned unit
    int unitId=0;


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
