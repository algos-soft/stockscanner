package com.algos.stockscanner.data.entity;

import javax.persistence.Entity;

import com.algos.stockscanner.data.AbstractEntity;
import java.time.LocalDate;

@Entity
public class Simulation extends AbstractEntity {

    private String indexCode;
    private LocalDate start_ts;
    private LocalDate end_ts;
    private Integer amount;
    private Integer leverage;
    private Integer width;
    private Integer balancing;
    private Integer num_buy;
    private Integer num_sell;
    private Integer pl_percent;

    public String getIndexCode() {
        return indexCode;
    }
    public void setIndex(String index) {
        this.indexCode = index;
    }
    public LocalDate getStart_ts() {
        return start_ts;
    }
    public void setStart_ts(LocalDate start_ts) {
        this.start_ts = start_ts;
    }
    public LocalDate getEnd_ts() {
        return end_ts;
    }
    public void setEnd_ts(LocalDate end_ts) {
        this.end_ts = end_ts;
    }
    public Integer getAmount() {
        return amount;
    }
    public void setAmount(Integer amount) {
        this.amount = amount;
    }
    public Integer getLeverage() {
        return leverage;
    }
    public void setLeverage(Integer leverage) {
        this.leverage = leverage;
    }
    public Integer getWidth() {
        return width;
    }
    public void setWidth(Integer width) {
        this.width = width;
    }
    public Integer getBalancing() {
        return balancing;
    }
    public void setBalancing(Integer balancing) {
        this.balancing = balancing;
    }
    public Integer getNum_buy() {
        return num_buy;
    }
    public void setNum_buy(Integer num_buy) {
        this.num_buy = num_buy;
    }
    public Integer getNum_sell() {
        return num_sell;
    }
    public void setNum_sell(Integer num_sell) {
        this.num_sell = num_sell;
    }
    public Integer getPl_percent() {
        return pl_percent;
    }
    public void setPl_percent(Integer pl_percent) {
        this.pl_percent = pl_percent;
    }

}
