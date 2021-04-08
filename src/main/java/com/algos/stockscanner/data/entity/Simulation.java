package com.algos.stockscanner.data.entity;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

import com.algos.stockscanner.data.AbstractEntity;
import java.time.LocalDate;

@Entity
public class Simulation extends AbstractEntity {

    // --- parameters
    @ManyToOne(fetch = FetchType.LAZY)
    private MarketIndex index;

    private LocalDate startTs;  // timestamp of first point scanned
    private LocalDate endTs;  // timestamp of last point scanned
    private Float initialAmount;  // initial amount
    private Integer leverage;
    private Float amplitude;    // amplitude of the oscillation max/min, in percent
    private Float balancing;  // balancing of amplitude between up and down phases: 0 = 50% up /50% down, 1 = 100% up, -1=100% down

    // ---- consolidated data
    private Float finalAmount;  // amount at the end of the simulation
    private Float totSpread;    // total amount of buy spreads paid
    private Float totCommission;    // total amount of commission paid
    private Integer numBuy;    // number of buy orders
    private Integer numSell;    // number of sell orders
    private Integer plPercent;  // profit/loss percentage
    private Integer numPointsScanned; // number of points scanned
    private Integer maxPointsHold;  // maximum number of points holding a position
    private Integer minPointsHold;  // minimum number of points holding a position
    private Integer totPointsHold;    // total points in holding position


    public MarketIndex getIndex() {
        return index;
    }

    public void setIndex(MarketIndex index) {
        this.index = index;
    }

    public LocalDate getStartTs() {
        return startTs;
    }

    public void setStartTs(LocalDate startTs) {
        this.startTs = startTs;
    }

    public LocalDate getEndTs() {
        return endTs;
    }

    public void setEndTs(LocalDate endTs) {
        this.endTs = endTs;
    }

    public Float getInitialAmount() {
        return initialAmount;
    }

    public void setInitialAmount(Float initialAmount) {
        this.initialAmount = initialAmount;
    }

    public Integer getLeverage() {
        return leverage;
    }

    public void setLeverage(Integer leverage) {
        this.leverage = leverage;
    }

    public Float getAmplitude() {
        return amplitude;
    }

    public void setAmplitude(Float amplitude) {
        this.amplitude = amplitude;
    }

    public Float getBalancing() {
        return balancing;
    }

    public void setBalancing(Float balancing) {
        this.balancing = balancing;
    }

    public Float getFinalAmount() {
        return finalAmount;
    }

    public void setFinalAmount(Float finalAmount) {
        this.finalAmount = finalAmount;
    }

    public Float getTotSpread() {
        return totSpread;
    }

    public void setTotSpread(Float totSpread) {
        this.totSpread = totSpread;
    }

    public Float getTotCommission() {
        return totCommission;
    }

    public void setTotCommission(Float totCommission) {
        this.totCommission = totCommission;
    }

    public Integer getNumBuy() {
        return numBuy;
    }

    public void setNumBuy(Integer numBuy) {
        this.numBuy = numBuy;
    }

    public Integer getNumSell() {
        return numSell;
    }

    public void setNumSell(Integer numSell) {
        this.numSell = numSell;
    }

    public Integer getPlPercent() {
        return plPercent;
    }

    public void setPlPercent(Integer plPercent) {
        this.plPercent = plPercent;
    }

    public Integer getNumPointsScanned() {
        return numPointsScanned;
    }

    public void setNumPointsScanned(Integer numPointsScanned) {
        this.numPointsScanned = numPointsScanned;
    }

    public Integer getMaxPointsHold() {
        return maxPointsHold;
    }

    public void setMaxPointsHold(Integer maxPointsHold) {
        this.maxPointsHold = maxPointsHold;
    }

    public Integer getMinPointsHold() {
        return minPointsHold;
    }

    public void setMinPointsHold(Integer minPointsHold) {
        this.minPointsHold = minPointsHold;
    }

    public Integer getTotPointsHold() {
        return totPointsHold;
    }

    public void setTotPointsHold(Integer totPointsHold) {
        this.totPointsHold = totPointsHold;
    }
}
