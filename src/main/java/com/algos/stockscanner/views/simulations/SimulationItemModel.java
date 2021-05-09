package com.algos.stockscanner.views.simulations;

import com.algos.stockscanner.enums.ActionTypes;
import com.algos.stockscanner.enums.Actions;
import com.algos.stockscanner.enums.Reasons;

import java.time.LocalDateTime;

/***
 * Model of a Simulation for the View
 */
public class SimulationItemModel {

    private int id;

    private LocalDateTime timestamp;
    private Actions action;
    private ActionTypes actionType;
    private Reasons reason;
    private float refPrice;
    private float currPrice;
    private float deltaAmpl;
    private float spreadAmt;
    private float commissionAmt;
    private float currValue;
    private float pl;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Actions getAction() {
        return action;
    }

    public void setAction(Actions action) {
        this.action = action;
    }

    public ActionTypes getActionType() {
        return actionType;
    }

    public void setActionType(ActionTypes actionType) {
        this.actionType = actionType;
    }

    public Reasons getReason() {
        return reason;
    }

    public void setReason(Reasons reason) {
        this.reason = reason;
    }

    public float getRefPrice() {
        return refPrice;
    }

    public void setRefPrice(float refPrice) {
        this.refPrice = refPrice;
    }

    public float getCurrPrice() {
        return currPrice;
    }

    public void setCurrPrice(float currPrice) {
        this.currPrice = currPrice;
    }

    public float getDeltaAmpl() {
        return deltaAmpl;
    }

    public void setDeltaAmpl(float deltaAmpl) {
        this.deltaAmpl = deltaAmpl;
    }

    public float getSpreadAmt() {
        return spreadAmt;
    }

    public void setSpreadAmt(float spreadAmt) {
        this.spreadAmt = spreadAmt;
    }

    public float getCommissionAmt() {
        return commissionAmt;
    }

    public void setCommissionAmt(float commissionAmt) {
        this.commissionAmt = commissionAmt;
    }

    public float getCurrValue() {
        return currValue;
    }

    public void setCurrValue(float currValue) {
        this.currValue = currValue;
    }

    public float getPl() {
        return pl;
    }

    public void setPl(float pl) {
        this.pl = pl;
    }
}
