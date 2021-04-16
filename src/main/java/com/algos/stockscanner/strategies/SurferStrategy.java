package com.algos.stockscanner.strategies;

import com.algos.stockscanner.beans.Utils;
import com.algos.stockscanner.data.enums.Actions;
import com.algos.stockscanner.data.enums.Reasons;
import com.algos.stockscanner.data.enums.Terminations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@Scope("prototype")
public class SurferStrategy extends AbsStrategy {

    @Autowired
    Utils utils;

    boolean amplitudeExceeded;

    float previousPrice;

    @Override
    public String getCode() {
        return "SURFER";
    }

    public SurferStrategy(StrategyParams params) {
        super(params);
    }


    @Override
    public void processUnit() throws Exception {
        super.processUnit();
        previousPrice=unit.getClose();
    }


    /**
     * Take a decision based on the current state
     */
    @Override
    public ActionReason takeDecision() {
        ActionReason actionReason;

        if(onHold){
            actionReason = decideOnHold();
        }else{
            actionReason = decideOnWait();
        }

        return actionReason;
    }

    /**
     * Take a decision when holding the assets
     */
    private ActionReason decideOnHold(){
        Actions action;
        Reasons reason=null;

        float currentPrice=unit.getClose();
        float deltaPercent=deltaPercent(buyPrice, currentPrice);
        float amplitude = simulation.getAmplitude();

        System.out.println("HOLDING - "+unit.getDateTime()+" "+currentPrice+" "+deltaPercent+"%");

        if(!amplitudeExceeded){
            if(deltaPercent>amplitude){
                amplitudeExceeded =true;
            }
            action=Actions.STAY;
        }else{  // amplitude already exceeded

            if (deltaPercent < amplitude) {   // back under amplitude, continue
                amplitudeExceeded =false;
                action=Actions.STAY;
            }else{  // still over amplitude, continue waiting until direction inverts
                if(currentPrice>previousPrice){   // keeps growing
                    action=Actions.STAY;
                }else{  // curve inverted
                    action=Actions.SELL;
                    reason=Reasons.ABOVE_THRESHOLD;
                }
                amplitudeExceeded =false;
            }

        }

        return new ActionReason(action, reason);

    }

    /**
     * Take a decision when waiting to buy
     */
    private ActionReason decideOnWait(){
        Actions action;
        Reasons reason=null;

        float currentPrice=unit.getClose();
        float deltaPercent=deltaPercent(avgBackPrice(), currentPrice);
        float amplitude = simulation.getAmplitude();

        System.out.println("WAITING - "+unit.getDateTime()+" "+currentPrice+" "+deltaPercent+"%");

        if(!amplitudeExceeded){
            if(deltaPercent<-amplitude){
                amplitudeExceeded =true;
            }
            action=Actions.STAY;
        }else{  // amplitude already exceeded

            if (deltaPercent > -amplitude) {   // back under amplitude, continue
                amplitudeExceeded =false;
                action=Actions.STAY;
            }else{  // still over amplitude, continue waiting until direction inverts
                if(currentPrice<previousPrice){   // keeps going down
                    action=Actions.STAY;
                }else{  // curve inverted
                    action=Actions.BUY;
                    reason=Reasons.BELOW_THRESHOLD;
                }
                amplitudeExceeded =false;
            }

        }

        return new ActionReason(action, reason);

    }



    /**
     * @return null if not finished, the termination reason if finished
     */
    @Override
    public Terminations isFinished() {

        // if 1) fixed days or 2) variable days with max specified, then check the max day
        boolean fixedDays = params.isFixedDays();
        LocalDate endDate = params.getEndDate();
        if (fixedDays || (!fixedDays && endDate != null)) {
            LocalDate unitDate = unit.getDateTimeLDT().toLocalDate();
            if (unitDate.isAfter(endDate)) {
                return Terminations.MAX_DAYS_REACHED;
            }
        }


        return null;
    }


}
