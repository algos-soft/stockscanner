package com.algos.stockscanner.strategies;

import com.algos.stockscanner.beans.Utils;
import com.algos.stockscanner.data.enums.*;
import com.algos.stockscanner.data.enums.Actions;
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
    public Decision takeDecision() {
        Decision decision;

        // price against which to evaluate the delta
        float refPrice;
        if(posOpen){
            refPrice = openPrice;
        }else{
            refPrice = avgBackPrice();
        }

        float deltaPercent=deltaPercent(refPrice, unit.getClose());

        System.out.println(unit.getDateTime()+" "+ posOpen +" "+refPrice+" "+unit.getClose()+" "+deltaPercent+"%");

        if(Math.abs(deltaPercent)>simulation.getAmplitude()){
            if(deltaPercent>0){ // overvalued, try to sell
                decision = decideSell(deltaPercent);
            }else{  // undervalued, try to buy
                decision = decideBuy(deltaPercent);
            }
        }else{
            //amplitudeExceeded =false;
            decision=new Decision(Actions.STAY, null, null);
        }

        System.out.println("    "+decision+" "+unit.getDateTime()+" "+refPrice+" "+unit.getClose()+" "+deltaPercent+"%");

        return decision;
    }

    /**
     * Take a decision when expecting to sell
     */
    private Decision decideSell(float deltaPercent){
        Actions action;
        ActionTypes actionType=null;
        Reasons reason=null;

        float currentPrice=unit.getClose();
        float amplitude = simulation.getAmplitude();

        if(!amplitudeExceeded){
            if(deltaPercent>amplitude){
                amplitudeExceeded =true;
            }
            action = Actions.STAY;
        }else{  // amplitude already exceeded

            if (deltaPercent < amplitude) {   // back under amplitude, continue
                amplitudeExceeded =false;
                action = Actions.STAY;
            }else{  // still over amplitude, continue waiting until direction inverts
                if(currentPrice>previousPrice){   // keeps growing
                    action= Actions.STAY;
                }else{  // curve inverted
                    action = Actions.OPEN;
                    actionType=ActionTypes.SELL;
                    reason=Reasons.ABOVE_THRESHOLD;
                }
                amplitudeExceeded =false;
            }

        }

        return new Decision(action, actionType, reason);

    }

    /**
     * Take a decision when expecting to buy
     */
    private Decision decideBuy(float deltaPercent){
        Actions action;
        ActionTypes actionType=null;
        Reasons reason=null;

        float currentPrice=unit.getClose();
        float amplitude = simulation.getAmplitude();

        if(!amplitudeExceeded){
            if(deltaPercent<-amplitude){
                amplitudeExceeded =true;
            }
            action= Actions.STAY;
        }else{  // amplitude already exceeded

            if (deltaPercent > -amplitude) {   // back under amplitude, continue
                amplitudeExceeded =false;
                action= Actions.STAY;
            }else{  // still over amplitude, continue waiting until direction inverts
                if(currentPrice<previousPrice){   // keeps going down
                    action= Actions.STAY;
                }else{  // curve inverted
                    action= Actions.OPEN;
                    actionType = ActionTypes.BUY;
                    reason=Reasons.BELOW_THRESHOLD;
                }
                amplitudeExceeded =false;
            }

        }

        return new Decision(action, actionType, reason);

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
