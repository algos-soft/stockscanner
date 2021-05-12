package com.algos.stockscanner.strategies;

public class SurferStrategyParams implements StrategyParams {

    private float amplitude;

    private int daysLookback;

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
