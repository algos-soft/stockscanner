package com.algos.stockscanner.views.simulations;

import com.vaadin.flow.component.html.Image;

import java.time.LocalDate;

/***
 * Model of a Simulation for the View
 */
public class SimulationModel {

    private int id;

    private int numGenerator;
    private String symbol;

    private LocalDate startTs;
    private LocalDate endTs;
    private float initialAmount;
    private float amplitude;
    private int daysLookback;
    private String terminationCode;
    private float totSpread;
    private float totCommission;
    private float pl;
    private float plPercent;
    private int numPointsScanned;
    private int numOpenings;
    private int numPointsHold;
    private int numPointsWait;
    private int minPointsHold;
    private int maxPointsHold;
    private Image image;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNumGenerator() {
        return numGenerator;
    }

    public void setNumGenerator(int numGenerator) {
        this.numGenerator = numGenerator;
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

    public String getTerminationCode() {
        return terminationCode;
    }

    public void setTerminationCode(String terminationCode) {
        this.terminationCode = terminationCode;
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

    public float getPl() {
        return pl;
    }

    public void setPl(float pl) {
        this.pl = pl;
    }

    public float getPlPercent() {
        return plPercent;
    }

    public void setPlPercent(float plPercent) {
        this.plPercent = plPercent;
    }

    public int getNumPointsScanned() {
        return numPointsScanned;
    }

    public void setNumPointsScanned(int numPointsScanned) {
        this.numPointsScanned = numPointsScanned;
    }

    public int getNumOpenings() {
        return numOpenings;
    }

    public void setNumOpenings(int numOpenings) {
        this.numOpenings = numOpenings;
    }

    public int getNumPointsHold() {
        return numPointsHold;
    }

    public void setNumPointsHold(int numPointsHold) {
        this.numPointsHold = numPointsHold;
    }

    public int getNumPointsWait() {
        return numPointsWait;
    }

    public void setNumPointsWait(int numPointsWait) {
        this.numPointsWait = numPointsWait;
    }

    public int getMinPointsHold() {
        return minPointsHold;
    }

    public void setMinPointsHold(int minPointsHold) {
        this.minPointsHold = minPointsHold;
    }

    public int getMaxPointsHold() {
        return maxPointsHold;
    }

    public void setMaxPointsHold(int maxPointsHold) {
        this.maxPointsHold = maxPointsHold;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }
}
