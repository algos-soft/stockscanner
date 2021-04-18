package com.algos.stockscanner.views.generators;

import com.vaadin.flow.component.html.Image;

import java.time.LocalDate;

/***
 * Model of a Generator for the View
 */
public class GeneratorModel {

    private int id;
    private int number;
    private Image image;
    private String symbol;
    private LocalDate startDate;
    private int days;
    private int spans;
    private int amount;
    private int stopLoss;
    private int takeProfit;
    private boolean durationFixed;

    private int amplitude;
    private boolean permutateAmpitude;
    private int amplitudeMin;    // min amplitude, percent
    private int amplitudeMax;    // max amplitude, percent
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

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
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

    public int getSpans() {
        return spans;
    }

    public void setSpans(int spans) {
        this.spans = spans;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getStopLoss() {
        return stopLoss;
    }

    public void setStopLoss(int stopLoss) {
        this.stopLoss = stopLoss;
    }

    public int getTakeProfit() {
        return takeProfit;
    }

    public void setTakeProfit(int takeProfit) {
        this.takeProfit = takeProfit;
    }

    public boolean isDurationFixed() {
        return durationFixed;
    }

    public void setDurationFixed(boolean durationFixed) {
        this.durationFixed = durationFixed;
    }

    public int getAmplitude() {
        return amplitude;
    }

    public void setAmplitude(int amplitude) {
        this.amplitude = amplitude;
    }

    public boolean isPermutateAmpitude() {
        return permutateAmpitude;
    }

    public void setPermutateAmpitude(boolean permutateAmpitude) {
        this.permutateAmpitude = permutateAmpitude;
    }

    public int getAmplitudeMin() {
        return amplitudeMin;
    }

    public void setAmplitudeMin(int amplitudeMin) {
        this.amplitudeMin = amplitudeMin;
    }

    public int getAmplitudeMax() {
        return amplitudeMax;
    }

    public void setAmplitudeMax(int amplitudeMax) {
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
