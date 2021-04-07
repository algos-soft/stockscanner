package com.algos.stockscanner.views.permutations;

import java.time.LocalDate;

/***
 * Model of a Permutation for the View
 */
public class PermutationModel {

    private int id;
    private byte[] imageData;
    private String symbol;
    private LocalDate startDate;
    private int days;
    private float amount;
    private int leverage;

    private float amplitude;
    private boolean permutateAmpitude;
    private float amplitudeMin;    // min amplitude, percent
    private float amplitudeMax;    // max amplitude, percent
    private int amplitudeSteps;    // how many steps

    private float balancing;
    private boolean permutateBalancing;
    private float balancingMin;  // 0 = 50% up /50% down, 1 = 100% up, -1=100% down
    private float balancingMax;  // 0 = 50% up /50% down, 1 = 100% up, -1=100% down
    private int balancingSteps;  // how many steps

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

    public float getBalancing() {
        return balancing;
    }

    public void setBalancing(float balancing) {
        this.balancing = balancing;
    }

    public boolean isPermutateBalancing() {
        return permutateBalancing;
    }

    public void setPermutateBalancing(boolean permutateBalancing) {
        this.permutateBalancing = permutateBalancing;
    }

    public float getBalancingMin() {
        return balancingMin;
    }

    public void setBalancingMin(float balancingMin) {
        this.balancingMin = balancingMin;
    }

    public float getBalancingMax() {
        return balancingMax;
    }

    public void setBalancingMax(float balancingMax) {
        this.balancingMax = balancingMax;
    }

    public int getBalancingSteps() {
        return balancingSteps;
    }

    public void setBalancingSteps(int balancingSteps) {
        this.balancingSteps = balancingSteps;
    }
}