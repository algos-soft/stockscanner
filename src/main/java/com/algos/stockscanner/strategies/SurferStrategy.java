package com.algos.stockscanner.strategies;

import com.algos.stockscanner.data.entity.IndexUnit;
import com.algos.stockscanner.data.entity.MarketIndex;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Scope("prototype")
public class SurferStrategy extends AbsStrategy {

    // Id of the currently scanned point
    private long currId;

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
     * Ensure that we have */
    private void ensureUnitsAvailable(){

        LocalDateTime dateTime= LocalDateTime.now().minusDays(30);
        MarketIndex index = getParams().getIndex();

        int id = indexUnitService.findFirstIdOf(index, dateTime);

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
