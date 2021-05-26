package com.algos.stockscanner.data.entity;

import com.algos.stockscanner.data.AbstractEntity;
import com.algos.stockscanner.utils.Du;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
public class SimulationItem extends AbstractEntity {

    @ManyToOne(fetch=FetchType.EAGER)
    private Simulation simulation;

    private String timestamp;

    private String action;

    private String actionType;

    private String reason;

    private Float refPrice;

    private Float currPrice;

    private Float deltaAmpl;

    private Float ampitudeUp;

    private Float ampitudeDn;

    private Float spreadAmt;

    private Float commissionAmt;

    private Float currValue;

    private Float pl;


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

    public Float getRefPrice() {
        return refPrice;
    }

    public void setRefPrice(Float refPrice) {
        this.refPrice = refPrice;
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

    public Float getAmpitudeUp() {
        return ampitudeUp;
    }

    public void setAmpitudeUp(Float ampitudeUp) {
        this.ampitudeUp = ampitudeUp;
    }

    public Float getAmpitudeDn() {
        return ampitudeDn;
    }

    public void setAmpitudeDn(Float ampitudeDn) {
        this.ampitudeDn = ampitudeDn;
    }

    public Float getSpreadAmt() {
        return spreadAmt;
    }

    public void setSpreadAmt(Float spreadAmt) {
        this.spreadAmt = spreadAmt;
    }

    public Float getCommissionAmt() {
        return commissionAmt;
    }

    public void setCommissionAmt(Float commissionAmt) {
        this.commissionAmt = commissionAmt;
    }

    public Float getCurrValue() {
        return currValue;
    }

    public void setCurrValue(Float currValue) {
        this.currValue = currValue;
    }

    public Float getPl() {
        return pl;
    }

    public void setPl(Float pl) {
        this.pl = pl;
    }


// -------------------

    public LocalDateTime getTimestampLDT(){
        return Du.toLocalDateTime(timestamp);
    }

    public void setTimestampLDT(LocalDateTime localDateTime) {
        this.timestamp = Du.toUtcString(localDateTime);
    }

}
