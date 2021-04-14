package com.algos.stockscanner.strategies;

import com.algos.stockscanner.data.entity.IndexUnit;
import com.algos.stockscanner.data.entity.MarketIndex;
import com.algos.stockscanner.data.service.IndexUnitService;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public abstract class AbsStrategy implements Strategy {

    private static final int PAGE_SIZE = 100;      // max units per page
    StrategyParams params;
    boolean abort=false;

    // current page of units scanned
    List<IndexUnit> unitsPage=new ArrayList<>();

    // Index of the current unit in the units page, 0 for the first
    int unitIndex=0;

    // Currently scanned unit
    IndexUnit unit;

    // Id of the currently scanned unit
//    int unitId=0;

    // the reason why has terminated
    String terminationCode;


    @Autowired
    IndexUnitService indexUnitService;

    public AbsStrategy(StrategyParams params) {
        this.params=params;
    }

    public void execute() throws Exception {

        while(!isFinished()){
            if(abort){
                break;
            }

            if(ensureUnitsAvailable()){

                IndexUnit u = unitsPage.get(unitIndex);
                this.unit=u;

                processUnit();

            }else{
                terminationCode="NO_MORE_UNITS";
                break;
            }


            //Thread.sleep(5);
            unitIndex++;

        }

    }


    /**
     * Ensure that we still have units available in the current page.
     * If not, load another page.
     */
    private boolean ensureUnitsAvailable(){

        MarketIndex index = getParams().getIndex();

        if(unitIndex<unitsPage.size()){   // requested unit index is available
            return true;
        }else{  // need to load a new page
            if(unit!=null){   // we have a previous unit, start from the next one
                int unitId=unit.getId();
                List<IndexUnit> units = indexUnitService.findAllByIndexFromId(index, unitId+1, PAGE_SIZE);
                unitsPage.clear();
                if(units.size()>0){
                    unitsPage.addAll(units);
                    unitIndex=0;
                    return true;
                }else{  // no more units available in the index
                    return false;
                }
            }else{  // no previous unit, it is the first time
                LocalDate date = getParams().getStartDate();
                int firstId = indexUnitService.findFirstIdOf(index, date);
                unitsPage.clear();
                if(firstId>0){
                    List<IndexUnit> units = indexUnitService.findAllByIndexFromId(index, firstId, PAGE_SIZE);
                    unitsPage.addAll(units);
                    return true;
                } else {
                    return false;
                }
            }
        }

    }

    @Override
    public void abort() {
        abort=true;
    }

    private StrategyParams getParams(){
        return params;
    }


}
