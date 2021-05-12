package com.algos.stockscanner.strategies;

import com.algos.stockscanner.beans.Utils;
import com.algos.stockscanner.enums.*;
import com.algos.stockscanner.enums.Actions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@Scope("prototype")
public class SurferStrategyOld extends AbsStrategyOld {

    @Autowired
    Utils utils;

    boolean amplitudeExceeded;

    private boolean preAlertBuy;

    private boolean preAlertSell;

    @Override
    public String getCode() {
        return "SURFER";
    }

    public SurferStrategyOld(StrategyParamsOld params) {
        super(params);
    }


    @Override
    public Decision decideIfOpenPosition() {
        Decision decision;

        float refPrice = avgBackPrice();
        float deltaPercent = strategyService.deltaPercent(refPrice, unit.getClose());

        if (Math.abs(deltaPercent) > simulation.getAmplitude()) {

            if (deltaPercent > 0) { // up above amplitude
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

            } else {   // down below amplitude
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
        } else {
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

        return decision;
    }

    @Override
    public void addProgressListener(StrategyProgressListener listener) {

    }


    /**
     * we have an open position of type BUY and we have to decide if closing it
     *
     * WARNING, CURRENT VARIABLES ARE NOT YET UPDATED
     */
    @Override
    public Decision decideIfCloseBuyPosition() {
        Decision decision;
        float refPrice = openPrice;
        float deltaPercent = strategyService.deltaPercent(refPrice, unit.getClose());

        Reasons sltpCondition = checkSlTp();
        if (sltpCondition == null) {
            if (deltaPercent > simulation.getAmplitude()) {
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

        return decision;
    }


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

        Reasons sltpCondition = checkSlTp();
        if (sltpCondition == null) {
            if (deltaPercent < -simulation.getAmplitude()) {
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

        return decision;
    }


    /**
     * Check SL/TP condition
     *
     * @return the reason (SL/TP) or null if not in SL/TP condition
     *
     * WARNING, CURRENT VARIABLES ARE NOT YET UPDATED
     */
    private Reasons checkSlTp() {

        // check stop loss
        Integer slPercent = params.getSl();
        if (slPercent != null && slPercent > 0) {
            if (posOpen) {
                float valueNow = lastValue + calcDeltaValue();
                float deltaPercent = strategyService.deltaPercent(openValue, valueNow);
                if (deltaPercent < -slPercent) {
                    return Reasons.STOP_LOSS_REACHED;
                }
            }
        }

        // check take profit
        Integer tpPercent = params.getTp();
        if (tpPercent != null && tpPercent > 0) {
            if (posOpen) {
                float valueNow = lastValue + calcDeltaValue();
                float deltaPercent = strategyService.deltaPercent(openValue, valueNow);
                if (deltaPercent > tpPercent) {
                    return Reasons.TAKE_PROFIT_REACHED;
                }
            }
        }

        return null;
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