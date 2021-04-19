package com.algos.stockscanner.strategies;

import com.algos.stockscanner.data.entity.MarketIndex;

import java.time.LocalDate;

public class StrategyParams {

    private MarketIndex index;

    private LocalDate startDate;

    private LocalDate endDate;

    private boolean fixedDays;

    float initialAmount;

    int sl;

    int tp;

    private float amplitude;

    private float spreadPercent;

    private int daysLookback;

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

    public int getSl() {
        return sl;
    }

    public void setSl(int sl) {
        this.sl = sl;
    }

    public int getTp() {
        return tp;
    }

    public void setTp(int tp) {
        this.tp = tp;
    }

    public float getAmplitude() {
        return amplitude;
    }

    public void setAmplitude(float amplitude) {
        this.amplitude = amplitude;
    }

    public float getSpreadPercent() {
        return spreadPercent;
    }

    public void setSpreadPercent(float spreadPercent) {
        this.spreadPercent = spreadPercent;
    }

    public int getDaysLookback() {
        return daysLookback;
    }

    public void setDaysLookback(int daysLookback) {
        this.daysLookback = daysLookback;
    }
}
