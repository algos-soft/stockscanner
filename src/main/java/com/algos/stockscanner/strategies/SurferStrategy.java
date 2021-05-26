package com.algos.stockscanner.strategies;

import com.algos.stockscanner.beans.Utils;
import com.algos.stockscanner.data.entity.MarketIndex;
import com.algos.stockscanner.data.entity.Simulation;
import com.algos.stockscanner.enums.ActionTypes;
import com.algos.stockscanner.enums.Actions;
import com.algos.stockscanner.enums.Reasons;
import com.algos.stockscanner.enums.Terminations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDate;

@Component("surferStrategy")
@Scope("prototype")
public class SurferStrategy extends AbsStrategy {

    @Autowired
    Utils utils;

    private int sl;

    float amplitude;
    float amplitudeDown;
    float amplitudeUp;

    int daysLookback;

//    boolean amplitudeExceeded;

    private boolean preAlertBuy;

    private boolean preAlertSell;

    @Override
    public String getCode() {
        return "SURFER";
    }

    public SurferStrategy(MarketIndex index, LocalDate startDate, int numDays, float initialAmount, int sl, float amplitude, int daysLookback) {
        super(index, startDate, numDays, initialAmount);
        this.sl=sl;
//        this.tp=tp;
        this.amplitude=amplitude;
        this.daysLookback=daysLookback;
    }

    @PostConstruct
    private void init(){
        this.amplitudeUp=amplitude;
        this.amplitudeDown=amplitude;
    }

    @Override
    public Simulation execute() throws Exception {
        Simulation simulation = super.execute();

        // add specific info
        simulation.setSl(sl);
        simulation.setAmplitude(amplitude);
        simulation.setDaysLookback(daysLookback);
        return  simulation;
    }

//    //@Override
//    public Decision decideIfOpenPositionOld() {
//        Decision decision;
//
//        float refPrice = movingAverage(daysLookback);
//        float deltaPercent = strategyService.deltaPercent(refPrice, unit.getClose());
//
//        if (Math.abs(deltaPercent) > amplitude) {
//
//            if (deltaPercent > 0) { // up above amplitude
//                if (!preAlertSell) {
//                    preAlertSell = true;
//                    decision = new Decision(Actions.STAY, null, Reasons.PRE_ALERT_SELL);
//                } else {
//                    if (unit.getClose() > lastPrice) {   // still growing
//                        decision = new Decision(Actions.STAY, null, Reasons.STILL_GOING_UP);
//                    } else {  // curve inverted
//                        decision = new Decision(Actions.OPEN, ActionTypes.SELL, Reasons.ABOVE_THRESHOLD);
//                        preAlertSell = false;
//                    }
//                }
//
//            } else {   // down below amplitude
//                if (!preAlertBuy) {
//                    preAlertBuy = true;
//                    decision = new Decision(Actions.STAY, null, Reasons.PRE_ALERT_BUY);
//                } else {
//                    if (unit.getClose() < lastPrice) {   // still going down
//                        decision = new Decision(Actions.STAY, null, Reasons.STILL_GOING_DOWN);
//                    } else {  // curve inverted
//                        decision = new Decision(Actions.OPEN, ActionTypes.BUY, Reasons.BELOW_THRESHOLD);
//                        preAlertBuy = false;
//                    }
//                }
//
//            }
//        } else {
//            decision = new Decision(Actions.STAY, null, Reasons.IN_BOUNDS);
//            preAlertSell = false;
//            preAlertBuy = false;
//        }
//
//        // enrich decision info
//        DecisionInfo info = decision.getDecisionInfo();
//        info.setRefPrice(refPrice);
//        info.setTimestamp(unit.getDateTime());
//        info.setCurrPrice(unit.getClose());
//        info.setDeltaAmpl(deltaPercent);
//
//        return decision;
//    }



    @Override
    public Decision decideIfOpenPosition() {
        Decision decision=null;

        float refPrice = movingAverage(daysLookback);
        float deltaPercent = strategyService.deltaPercent(refPrice, unit.getClose());

        if(deltaPercent>0){ // moving up
            if(deltaPercent>amplitudeUp){// up above amplitude
                if (!preAlertSell) {
                    preAlertSell = true;
                    decision = new Decision(Actions.STAY, null, Reasons.PRE_ALERT_SELL);
                } else {
                    if (unit.getClose() > lastPrice) {   // still growing
                        decision = new Decision(Actions.STAY, null, Reasons.STILL_GOING_UP);
                    } else {  // curve inverted
                        decision = new Decision(Actions.OPEN, ActionTypes.SELL, Reasons.ABOVE_THRESHOLD);
                        preAlertSell = false;
                    }
                }
            }
        }else{  // moving down
            if(deltaPercent<-amplitudeDown){  // down below amplitude
                if (!preAlertBuy) {
                    preAlertBuy = true;
                    decision = new Decision(Actions.STAY, null, Reasons.PRE_ALERT_BUY);
                } else {
                    if (unit.getClose() < lastPrice) {   // still going down
                        decision = new Decision(Actions.STAY, null, Reasons.STILL_GOING_DOWN);
                    } else {  // curve inverted
                        decision = new Decision(Actions.OPEN, ActionTypes.BUY, Reasons.BELOW_THRESHOLD);
                        preAlertBuy = false;
                    }
                }
            }
        }

        if(decision==null){
            decision = new Decision(Actions.STAY, null, Reasons.IN_BOUNDS);
            preAlertSell = false;
            preAlertBuy = false;
        }

        // enrich decision info
        DecisionInfo info = decision.getDecisionInfo();
        info.setRefPrice(refPrice);
        info.setTimestamp(unit.getDateTime());
        info.setCurrPrice(unit.getClose());
        info.setDeltaAmpl(deltaPercent);
        info.setAmplitudeUp(amplitudeUp);
        info.setAmplitudeDn(amplitudeDown);

        return decision;
    }



//    /**
//     * we have an open position of type BUY and we have to decide if closing it
//     *
//     * WARNING, CURRENT VARIABLES ARE NOT YET UPDATED
//     */
//    //@Override
//    public Decision decideIfCloseBuyPositionOld() {
//        Decision decision;
//        float refPrice = openPrice;
//        float deltaPercent = strategyService.deltaPercent(refPrice, unit.getClose());
//
//        Reasons sltpCondition = checkStopLoss();
//        if (sltpCondition == null) {
//            if (deltaPercent > amplitude) {
//                if (!preAlertSell) {
//                    preAlertSell = true;
//                    decision = new Decision(Actions.STAY, null, Reasons.PRE_ALERT_CLOSE);
//                } else {
//                    if (unit.getClose() > lastPrice) {   // still growing
//                        decision = new Decision(Actions.STAY, null, Reasons.STILL_GOING_UP);
//                    } else {  // curve inverted
//                        decision = new Decision(Actions.CLOSE, null, Reasons.ABOVE_THRESHOLD);
//                        preAlertSell = false;
//                    }
//                }
//            } else {
//                decision = new Decision(Actions.STAY, null, Reasons.IN_BOUNDS);
//                preAlertSell = false;
//            }
//        } else {
//            decision = new Decision(Actions.CLOSE, null, sltpCondition);
//        }
//
//        // enrich decision info
//        DecisionInfo info = decision.getDecisionInfo();
//        info.setRefPrice(refPrice);
//        info.setTimestamp(unit.getDateTime());
//        info.setCurrPrice(unit.getClose());
//        info.setDeltaAmpl(deltaPercent);
//
//        return decision;
//    }


    /**
     * we hold an open position of type BUY and we have to decide if closing it
     *
     * WARNING, CURRENT VARIABLES ARE NOT YET UPDATED
     */
    @Override
    public Decision decideIfCloseBuyPosition() {
        Decision decision;
        float refPrice = openPrice;
        float deltaPercent = strategyService.deltaPercent(refPrice, unit.getClose());

        Reasons sltpCondition = checkStopLoss();
        if (sltpCondition == null) {
            if (deltaPercent > amplitudeUp) {
                if (!preAlertSell) {
                    preAlertSell = true;
                    decision = new Decision(Actions.STAY, null, Reasons.PRE_ALERT_CLOSE);
                } else {
                    if (unit.getClose() > lastPrice) {   // still growing
                        decision = new Decision(Actions.STAY, null, Reasons.STILL_GOING_UP);
                    } else {  // curve inverted
                        decision = new Decision(Actions.CLOSE, null, Reasons.ABOVE_THRESHOLD);
                        preAlertSell = false;
                    }
                }
            } else {
                decision = new Decision(Actions.STAY, null, Reasons.IN_BOUNDS);
                preAlertSell = false;
            }
        } else {
            decision = new Decision(Actions.CLOSE, null, sltpCondition);
        }

        // enrich decision info
        DecisionInfo info = decision.getDecisionInfo();
        info.setRefPrice(refPrice);
        info.setTimestamp(unit.getDateTime());
        info.setCurrPrice(unit.getClose());
        info.setDeltaAmpl(deltaPercent);
        info.setAmplitudeUp(amplitudeUp);
        info.setAmplitudeDn(amplitudeDown);

        return decision;
    }



//    /**
//     * we have an open position of type SELL and we have to decide if closing it
//     *
//     * WARNING, CURRENT VARIABLES ARE NOT YET UPDATED
//     */
//    //@Override
//    public Decision decideIfCloseSellPositionOld() {
//        Decision decision;
//        float refPrice = openPrice;
//        float deltaPercent = strategyService.deltaPercent(refPrice, unit.getClose());
//
//        Reasons sltpCondition = checkStopLoss();
//        if (sltpCondition == null) {
//            if (deltaPercent < -amplitude) {
//                if (!preAlertSell) {
//                    preAlertSell = true;
//                    decision = new Decision(Actions.STAY, null, Reasons.PRE_ALERT_CLOSE);
//                } else {
//                    if (unit.getClose() < lastPrice) {   // still going down
//                        decision = new Decision(Actions.STAY, null, Reasons.STILL_GOING_DOWN);
//                    } else {  // curve inverted
//                        decision = new Decision(Actions.CLOSE, null, Reasons.BELOW_THRESHOLD);
//                        preAlertSell = false;
//                    }
//                }
//            } else {
//                decision = new Decision(Actions.STAY, null, Reasons.IN_BOUNDS);
//                preAlertSell = false;
//            }
//        } else {
//            decision = new Decision(Actions.CLOSE, null, sltpCondition);
//        }
//
//        // enrich decision info
//        DecisionInfo info = decision.getDecisionInfo();
//        info.setRefPrice(refPrice);
//        info.setTimestamp(unit.getDateTime());
//        info.setCurrPrice(unit.getClose());
//        info.setDeltaAmpl(deltaPercent);
//
//        return decision;
//    }

    /**
     * we have an open position of type SELL and we have to decide if closing it
     *
     * WARNING, CURRENT VARIABLES ARE NOT YET UPDATED
     */
    @Override
    public Decision decideIfCloseSellPosition() {
        Decision decision;
        float refPrice = openPrice;
        float deltaPercent = strategyService.deltaPercent(refPrice, unit.getClose());

        Reasons sltpCondition = checkStopLoss();
        if (sltpCondition == null) {
            if (deltaPercent < -amplitudeDown) {
                if (!preAlertSell) {
                    preAlertSell = true;
                    decision = new Decision(Actions.STAY, null, Reasons.PRE_ALERT_CLOSE);
                } else {
                    if (unit.getClose() < lastPrice) {   // still going down
                        decision = new Decision(Actions.STAY, null, Reasons.STILL_GOING_DOWN);
                    } else {  // curve inverted
                        decision = new Decision(Actions.CLOSE, null, Reasons.BELOW_THRESHOLD);
                        preAlertSell = false;
                    }
                }
            } else {
                decision = new Decision(Actions.STAY, null, Reasons.IN_BOUNDS);
                preAlertSell = false;
            }
        } else {
            decision = new Decision(Actions.CLOSE, null, sltpCondition);
        }

        // enrich decision info
        DecisionInfo info = decision.getDecisionInfo();
        info.setRefPrice(refPrice);
        info.setTimestamp(unit.getDateTime());
        info.setCurrPrice(unit.getClose());
        info.setDeltaAmpl(deltaPercent);
        info.setAmplitudeUp(amplitudeUp);
        info.setAmplitudeDn(amplitudeDown);

        return decision;
    }



    /**
     * Check SL condition
     *
     * @return the reason (SL) or null if not in SL condition
     *
     * WARNING, CURRENT VARIABLES ARE NOT YET UPDATED
     */
    private Reasons checkStopLoss() {

        // check stop loss
        Integer slPercent = sl;
        if (slPercent != null && slPercent > 0) {
            if (posOpen) {
                float valueNow = lastValue + calcDeltaValue();
                float deltaPercent = strategyService.deltaPercent(openValue, valueNow);
                if (deltaPercent < -slPercent) {
                    return Reasons.STOP_LOSS_REACHED;
                }
            }
        }

//        // check take profit
//        Integer tpPercent = tp;
//        if (tpPercent != null && tpPercent > 0) {
//            if (posOpen) {
//                float valueNow = lastValue + calcDeltaValue();
//                float deltaPercent = strategyService.deltaPercent(openValue, valueNow);
//                if (deltaPercent > tpPercent) {
//                    return Reasons.TAKE_PROFIT_REACHED;
//                }
//            }
//        }

        return null;
    }

    /**
     * @return null if not finished, the termination reason if finished
     */
    @Override
    public Terminations isFinished() {

        LocalDate unitDate = unit.getDateTimeLDT().toLocalDate();
        if (unitDate.isAfter(endDate)) {
            return Terminations.MAX_DAYS_REACHED;
        }

        return null;
    }


    @Override
    public String toString() {
        return(index.getSymbol()+", a="+amplitude+", d="+daysLookback);
    }

}
