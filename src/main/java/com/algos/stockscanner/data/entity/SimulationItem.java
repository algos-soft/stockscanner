package com.algos.stockscanner.data.entity;

import com.algos.stockscanner.data.AbstractEntity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
public class SimulationItem extends AbstractEntity {

    @ManyToOne(fetch = FetchType.EAGER)
    private Simulation simulation;

    private String timestamp;

    private String action;  // the action taken

    private String reason;  // the reason tor the action

    private Float amount;

    private Float price;

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

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Float getAmount() {
        return amount;
    }

    public void setAmount(Float amount) {
        this.amount = amount;
    }

    public Float getPrice() {
        return price;
    }

    public void setPrice(Float price) {
        this.price = price;
    }
}
