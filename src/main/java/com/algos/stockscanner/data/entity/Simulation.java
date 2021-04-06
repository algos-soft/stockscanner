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
    private MarketIndex marketIndex;

    private LocalDate startTs;  // timestamp of first point scanned
    private LocalDate endTs;  // timestamp of last point scanned
    private float initialAmount;  // initial amount
    private int leverage;
    private float amplitude;    // amplitude of the oscillation max/min, in percent
    private float balancing;  // balancing of amplitude between up and down phases: 0 = 50% up /50% down, 1 = 100% up, -1=100% down

    // ---- consolidated data
    private float finalAmount;  // amount at the end of the simulation
    private float totSpread;    // total amount of buy spreads paid
    private float totCommission;    // total amount of commission paid
    private int numBuy;    // number of buy orders
    private int numSell;    // number of sell orders
    private int plPercent;  // profit/loss percentage
    private int numPointsScanned; // number of points scanned
    private int maxPointsHold;  // maximum number of points holding a position
    private int minPointsHold;  // minimum number of points holding a position
    private int totPointsHold;    // total points in holding position


    public MarketIndex getMarketIndex() {
        return marketIndex;
    }

    public void setMarketIndex(MarketIndex marketIndex) {
        this.marketIndex = marketIndex;
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
    public float getInitialAmount() {
        return initialAmount;
    }
    public void setInitialAmount(float amount) {
        this.initialAmount = amount;
    }
    public int getLeverage() {
        return leverage;
    }
    public void setLeverage(int leverage) {
        this.leverage = leverage;
    }

    public float getAmplitude() {
        return amplitude;
    }

    public void setAmplitude(float amplitude) {
        this.amplitude = amplitude;
    }

    public float getBalancing() {
        return balancing;
    }
    public void setBalancing(float balancing) {
        this.balancing = balancing;
    }





    public float getFinalAmount() {
        return finalAmount;
    }

    public void setFinalAmount(float finalAmount) {
        this.finalAmount = finalAmount;
    }

    public float getTotSpread() {
        return totSpread;
    }

    public void setTotSpread(float totSpread) {
        this.totSpread = totSpread;
    }

    public float getTotCommission() {
        return totCommission;
    }

    public void setTotCommission(float totCommission) {
        this.totCommission = totCommission;
    }

    public int getNumBuy() {
        return numBuy;
    }

    public void setNumBuy(int numBuy) {
        this.numBuy = numBuy;
    }

    public int getNumSell() {
        return numSell;
    }

    public void setNumSell(int numSell) {
        this.numSell = numSell;
    }

    public int getPlPercent() {
        return plPercent;
    }

    public void setPlPercent(int plPercent) {
        this.plPercent = plPercent;
    }

    public int getNumPointsScanned() {
        return numPointsScanned;
    }

    public void setNumPointsScanned(int numPointsScanned) {
        this.numPointsScanned = numPointsScanned;
    }

    public int getMaxPointsHold() {
        return maxPointsHold;
    }

    public void setMaxPointsHold(int maxPointsHold) {
        this.maxPointsHold = maxPointsHold;
    }

    public int getMinPointsHold() {
        return minPointsHold;
    }

    public void setMinPointsHold(int minPointsHold) {
        this.minPointsHold = minPointsHold;
    }

    public int getTotPointsHold() {
        return totPointsHold;
    }

    public void setTotPointsHold(int totPointsHold) {
        this.totPointsHold = totPointsHold;
    }
}
