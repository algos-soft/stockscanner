package com.algos.stockscanner.views.simulations;

import java.time.LocalDate;

/***
 * Model of a Simulation for the View
 */
public class SimulationModel {

    private int id;
    private byte[] imageData;
    private String symbol;
    private LocalDate startTs;
    private LocalDate endTs;
    private float initialAmount;
    private int leverage;
    private float amplitude;
    private float balancing;
    private float finalAmount;
    private float totSpread;
    private float totCommission;
    private int numBuy;
    private int numSell;
    private int plPercent;
    private int numPointsScanned;
    private int maxPointsHold;
    private int minPointsHold;
    private int totPointsHold;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public byte[] getImageData() {
        return imageData;
    }

    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
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

    public void setInitialAmount(float initialAmount) {
        this.initialAmount = initialAmount;
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
