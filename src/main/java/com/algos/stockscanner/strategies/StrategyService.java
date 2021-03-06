package com.algos.stockscanner.strategies;

import com.algos.stockscanner.data.entity.IndexUnit;
import com.algos.stockscanner.data.entity.Simulation;
import com.algos.stockscanner.data.entity.SimulationItem;
import org.springframework.stereotype.Service;


@Service
public class StrategyService {


    /**
     * delta percent between the first and the second number
     */
    public float deltaPercent(float first, float second){
        if(first==0){
            return 0;
        }
        float deltaPercent = (second - first) * 100 / first;
        return deltaPercent;
    }

    /**
     * return the value with the given percentage applied
     */
    public float applyPercent(float value, float percent){
        return value+(value/100*percent);
    }

    /**
     * return the percentage of a value
     */
    public float calcPercent(float value, float percent){
        return value/100*percent;
    }



    public SimulationItem buildSimulationItem(Simulation simulation, Decision decision, IndexUnit unit){

        SimulationItem item = new SimulationItem();
        item.setSimulation(simulation);
        item.setTimestamp(unit.getDateTime());
        if(decision.getAction()!=null){
            item.setAction(decision.getAction().getCode());
        }
        if(decision.getActionType()!=null){
            item.setActionType(decision.getActionType().getCode());
        }
        if(decision.getReason()!=null){
            item.setReason(decision.getReason().getCode());
        }
        DecisionInfo info = decision.getDecisionInfo();
        if(info!=null){
            item.setRefPrice(info.getRefPrice());
            item.setCurrPrice(info.getCurrPrice());
            item.setDeltaAmpl(info.getDeltaAmpl());
            item.setAmpitudeUp(info.getAmplitudeUp());
            item.setAmpitudeDn(info.getAmplitudeDn());
            item.setPl(info.getPl());
            item.setSpreadAmt(info.getSpreadAmt());
        }
        item.setCurrValue(info.getCurrValue());

        return item;
    }


}
