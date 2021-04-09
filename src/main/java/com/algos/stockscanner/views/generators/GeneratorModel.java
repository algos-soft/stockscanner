package com.algos.stockscanner.views.generators;

import com.vaadin.flow.component.html.Image;

import java.time.LocalDate;

/***
 * Model of a Generator for the View
 */
public class GeneratorModel {

    private int id;
    private Image image;
    private String symbol;
    private LocalDate startDate;
    private int days;
    private float amount;
    private float stopLoss;
    private float takeProfit;
    private int leverage;
    private boolean durationFixed;

    private float amplitude;
    private boolean permutateAmpitude;
    private float amplitudeMin;    // min amplitude, percent
    private float amplitudeMax;    // max amplitude, percent
    private int amplitudeSteps;    // how many steps

    private int daysLookback;
    private boolean permutateDaysLookback;
    private int daysLookbackMin;
    private int daysLookbackMax;
    private int daysLookbackSteps;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public float getStopLoss() {
        return stopLoss;
    }

    public void setStopLoss(float stopLoss) {
        this.stopLoss = stopLoss;
    }

    public float getTakeProfit() {
        return takeProfit;
    }

    public void setTakeProfit(float takeProfit) {
        this.takeProfit = takeProfit;
    }

    public int getLeverage() {
        return leverage;
    }

    public void setLeverage(int leverage) {
        this.leverage = leverage;
    }

    public boolean isDurationFixed() {
        return durationFixed;
    }

    public void setDurationFixed(boolean durationFixed) {
        this.durationFixed = durationFixed;
    }

    public float getAmplitude() {
        return amplitude;
    }

    public void setAmplitude(float amplitude) {
        this.amplitude = amplitude;
    }

    public boolean isPermutateAmpitude() {
        return permutateAmpitude;
    }

    public void setPermutateAmpitude(boolean permutateAmpitude) {
        this.permutateAmpitude = permutateAmpitude;
    }

    public float getAmplitudeMin() {
        return amplitudeMin;
    }

    public void setAmplitudeMin(float amplitudeMin) {
        this.amplitudeMin = amplitudeMin;
    }

    public float getAmplitudeMax() {
        return amplitudeMax;
    }

    public void setAmplitudeMax(float amplitudeMax) {
        this.amplitudeMax = amplitudeMax;
    }

    public int getAmplitudeSteps() {
        return amplitudeSteps;
    }

    public void setAmplitudeSteps(int amplitudeSteps) {
        this.amplitudeSteps = amplitudeSteps;
    }

    public int getDaysLookback() {
        return daysLookback;
    }

    public void setDaysLookback(int daysLookback) {
        this.daysLookback = daysLookback;
    }

    public boolean isPermutateDaysLookback() {
        return permutateDaysLookback;
    }

    public void setPermutateDaysLookback(boolean permutateDaysLookback) {
        this.permutateDaysLookback = permutateDaysLookback;
    }

    public int getDaysLookbackMin() {
        return daysLookbackMin;
    }

    public void setDaysLookbackMin(int daysLookbackMin) {
        this.daysLookbackMin = daysLookbackMin;
    }

    public int getDaysLookbackMax() {
        return daysLookbackMax;
    }

    public void setDaysLookbackMax(int daysLookbackMax) {
        this.daysLookbackMax = daysLookbackMax;
    }

    public int getDaysLookbackSteps() {
        return daysLookbackSteps;
    }

    public void setDaysLookbackSteps(int daysLookbackSteps) {
        this.daysLookbackSteps = daysLookbackSteps;
    }
}
