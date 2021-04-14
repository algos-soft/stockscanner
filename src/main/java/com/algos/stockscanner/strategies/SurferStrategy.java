package com.algos.stockscanner.strategies;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class SurferStrategy extends AbsStrategy {

    @Override
    public String getCode() {
        return "SURFER";
    }

    private int loops=0;

    public SurferStrategy(StrategyParams params) {
        super(params);
    }

    @Override
    public void execute() throws Exception {

        while(!isFinished()){
            if(abort){
                break;
            }
            Thread.sleep(5);

            loops++;
        }

    }

    @Override
    public boolean isFinished() {
        return loops>=100;
    }


}
