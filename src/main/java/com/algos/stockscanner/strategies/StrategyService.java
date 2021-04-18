package com.algos.stockscanner.strategies;

import com.algos.stockscanner.data.entity.IndexUnit;
import com.algos.stockscanner.data.entity.Simulation;
import com.algos.stockscanner.data.entity.SimulationItem;
import com.algos.stockscanner.data.enums.Reasons;
import com.algos.stockscanner.data.enums.Terminations;
import org.springframework.stereotype.Service;


@Service
public class StrategyService {


    /**
     * delta percent between the first and the secon number
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
            item.setPl(info.getPl());
        }
        item.setCurrValue(info.getCurrValue());

        return item;
    }


    public Reasons terminationToReason(Terminations term) {
        Reasons reasons;
        switch (term){
            case ABORTED_BY_USER:
                reasons=Reasons.ABORTED_BY_USER;
                break;
            case MAX_DAYS_REACHED:
                reasons=Reasons.MAX_DAYS_REACHED;
                break;
            case NO_MORE_DATA:
                reasons=Reasons.NO_MORE_DATA;
                break;
            default:
                reasons=Reasons.UNKNOWN;
                break;
        }
        return reasons;
    }
}
