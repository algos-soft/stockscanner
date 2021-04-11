package com.algos.stockscanner.data.entity;

import com.algos.stockscanner.data.AbstractEntity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
public class SimulationItem extends AbstractEntity {

    @ManyToOne(fetch = FetchType.EAGER)
    private Simulation simulation;

    private LocalDateTime timestamp;

    private String action;  // the action taken

    private String reason;  // the reason tor the action

    private Float amount;

    private Float price;

}
