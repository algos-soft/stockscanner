package com.algos.stockscanner.strategies;

import com.algos.stockscanner.data.entity.IndexUnit;
import com.algos.stockscanner.data.entity.MarketIndex;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Scope("prototype")
public class SurferStrategy extends AbsStrategy {

//    // Id of the currently scanned unit
//    private long currUnitId;


    private int loops=0;



    @Override
    public String getCode() {
        return "SURFER";
    }


    public SurferStrategy(StrategyParams params) {
        super(params);
    }

    @Override
    public void execute() throws Exception {

        ensureUnitsAvailable();

        while(!isFinished()){
            if(abort){
                break;
            }

            // make sure we have more points to scan



            Thread.sleep(5);

            loops++;
        }

    }

    /**
     * Ensure that we have units available in the current page
     */
    private void ensureUnitsAvailable(){

        if(unitIndex<unitsPage.size()-1){   // at least 1 unit is available
            return;
        }else{  // need to load a new page
            if(unitId>0){   // we have a previous id, start from the next one

            }else{  // no previous id, it is the first time
                LocalDate date = getParams().getStartDate();
                MarketIndex index = getParams().getIndex();

            }
        }

        LocalDateTime dateTime= LocalDateTime.now().minusDays(30);
        MarketIndex index = getParams().getIndex();

        int id = indexUnitService.findFirstIdOf(index, dateTime);
        if(id>0){

        }else{

        }

        List<IndexUnit> units = indexUnitService.findAllByIndexWithDateTimeEqualOrAfter(index, dateTime);
        int a = 87;
        int b=a;


    }

    @Override
    public boolean isFinished() {
        return loops>=100;
    }

    private SurferStrategyParams getParams(){
        return (SurferStrategyParams)params;
    }

}
