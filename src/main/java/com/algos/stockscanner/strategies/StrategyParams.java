package com.algos.stockscanner.strategies;

import com.algos.stockscanner.data.entity.Generator;
import com.algos.stockscanner.data.entity.MarketIndex;

import java.time.LocalDate;

public class StrategyParams {

    private Generator generator;

    private MarketIndex index;

    private LocalDate startDate;

    private LocalDate endDate;

    private boolean fixedDays;

    float initialAmount;

    int leverage;

    float sl;

    float tp;

    private float amplitude;

    private int daysLookback;

    public Generator getGenerator() {
        return generator;
    }

    public void setGenerator(Generator generator) {
        this.generator = generator;
    }

    public MarketIndex getIndex() {
        return index;
    }

    public void setIndex(MarketIndex index) {
        this.index = index;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public boolean isFixedDays() {
        return fixedDays;
    }

    public void setFixedDays(boolean fixedDays) {
        this.fixedDays = fixedDays;
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

    public float getSl() {
        return sl;
    }

    public void setSl(float sl) {
        this.sl = sl;
    }

    public float getTp() {
        return tp;
    }

    public void setTp(float tp) {
        this.tp = tp;
    }

    public float getAmplitude() {
        return amplitude;
    }

    public void setAmplitude(float amplitude) {
        this.amplitude = amplitude;
    }

    public int getDaysLookback() {
        return daysLookback;
    }

    public void setDaysLookback(int daysLookback) {
        this.daysLookback = daysLookback;
    }
}
