package com.algos.stockscanner.data.entity;

import com.algos.stockscanner.data.AbstractEntity;

import javax.persistence.*;

@Entity
public class SimulationItem extends AbstractEntity {

    @ManyToOne(fetch = FetchType.EAGER)
    private Simulation simulation;

    private String timestamp;

    private String action;

    private String actionType;

    private String reason;

    private Float avgBack;

    private Float currPrice;

    private Float deltaAmpl;

    private Float currAmount;


    public Simulation getSimulation() {
        return simulation;
    }

    public void setSimulation(Simulation simulation) {
        this.simulation = simulation;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Float getAvgBack() {
        return avgBack;
    }

    public void setAvgBack(Float avgBack) {
        this.avgBack = avgBack;
    }

    public Float getCurrPrice() {
        return currPrice;
    }

    public void setCurrPrice(Float currPrice) {
        this.currPrice = currPrice;
    }

    public Float getDeltaAmpl() {
        return deltaAmpl;
    }

    public void setDeltaAmpl(Float deltaAmpl) {
        this.deltaAmpl = deltaAmpl;
    }

    public Float getCurrAmount() {
        return currAmount;
    }

    public void setCurrAmount(Float currAmount) {
        this.currAmount = currAmount;
    }

}
