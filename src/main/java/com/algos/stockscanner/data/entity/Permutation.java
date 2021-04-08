package com.algos.stockscanner.data.entity;

import com.algos.stockscanner.data.AbstractEntity;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import java.time.LocalDate;

@Entity
public class Permutation extends AbstractEntity {

    // fixed properties
    @ManyToOne(fetch = FetchType.LAZY)
    private MarketIndex marketIndex;
    private LocalDate startDate;  // start date
    private Integer days=0;   // number of days
    private Float amount=0f;  // amount invested
    private Integer leverage=1;

    // permutable properties

    private Float amplitude=0f;
    private Boolean permutateAmpitude=false;
    private Float amplitudeMin=0f;    // min amplitude, percent
    private Float amplitudeMax=0f;    // max amplitude, percent
    private Integer amplitudeSteps=0;    // how many steps

    private Float balancing=0f;
    private Boolean permutateBalancing=false;
    private Float balancingMin=0f;  // 0 = 50% up /50% down, 1 = 100% up, -1=100% down
    private Float balancingMax=0f;  // 0 = 50% up /50% down, 1 = 100% up, -1=100% down
    private Integer balancingSteps=0;  // how many steps


    public MarketIndex getMarketIndex() {
        return marketIndex;
    }

    public void setMarketIndex(MarketIndex marketIndex) {
        this.marketIndex = marketIndex;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public Integer getDays() {
        return days;
    }

    public void setDays(Integer days) {
        this.days = days;
    }

    public Float getAmount() {
        return amount;
    }

    public void setAmount(Float amount) {
        this.amount = amount;
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

    public Boolean getPermutateAmpitude() {
        return permutateAmpitude;
    }

    public void setPermutateAmpitude(Boolean permutateAmpitude) {
        this.permutateAmpitude = permutateAmpitude;
    }

    public Float getAmplitudeMin() {
        return amplitudeMin;
    }

    public void setAmplitudeMin(Float amplitudeMin) {
        this.amplitudeMin = amplitudeMin;
    }

    public Float getAmplitudeMax() {
        return amplitudeMax;
    }

    public void setAmplitudeMax(Float amplitudeMax) {
        this.amplitudeMax = amplitudeMax;
    }

    public Integer getAmplitudeSteps() {
        return amplitudeSteps;
    }

    public void setAmplitudeSteps(Integer amplitudeSteps) {
        this.amplitudeSteps = amplitudeSteps;
    }

    public Float getBalancing() {
        return balancing;
    }

    public void setBalancing(Float balancing) {
        this.balancing = balancing;
    }

    public Boolean getPermutateBalancing() {
        return permutateBalancing;
    }

    public void setPermutateBalancing(Boolean permutateBalancing) {
        this.permutateBalancing = permutateBalancing;
    }

    public Float getBalancingMin() {
        return balancingMin;
    }

    public void setBalancingMin(Float balancingMin) {
        this.balancingMin = balancingMin;
    }

    public Float getBalancingMax() {
        return balancingMax;
    }

    public void setBalancingMax(Float balancingMax) {
        this.balancingMax = balancingMax;
    }

    public Integer getBalancingSteps() {
        return balancingSteps;
    }

    public void setBalancingSteps(Integer balancingSteps) {
        this.balancingSteps = balancingSteps;
    }
}
