package com.algos.stockscanner.data.entity;

import com.algos.stockscanner.data.AbstractEntity;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
public class Generator extends AbstractEntity {

    private LocalDateTime created;
    private LocalDateTime modified;

    // fixed properties
    @ManyToOne(fetch = FetchType.EAGER)
    private MarketIndex index;
    private LocalDate startDate;  // start date
    private Boolean fixedDays;  // true for fixed number of days, false to go or until TP/SL or until end of index data
    private Integer days;   // number of days
    private Float amount;  // amount invested
    private Integer leverage;
    private Float stopLoss; // of the whole operation, not of the single shot
    private Float takeProfit; // of the whole operation, not of the single shot

    // permutable properties

    private Float amplitude;
    private Boolean amplitudePermutate;
    private Float amplitudeMin;    // min amplitude, percent
    private Float amplitudeMax;    // max amplitude, percent
    private Integer amplitudeSteps;    // how many steps

//    private Float balancing;
//    private Boolean permutateBalancing;
//    private Float balancingMin;  // 0 = 50% up /50% down, 1 = 100% up, -1=100% down
//    private Float balancingMax;  // 0 = 50% up /50% down, 1 = 100% up, -1=100% down
//    private Integer balancingSteps;  // how many steps

    private Integer avgDays;    // number of days taken in account to calculate the starting average value
    private Boolean avgDaysPermutate;
    private Integer avgDaysMin;
    private Integer avgDaysMax;
    private Integer avgDaysSteps;   // must be divisor of maxAvgDays-minAvgDays

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public LocalDateTime getModified() {
        return modified;
    }

    public void setModified(LocalDateTime modified) {
        this.modified = modified;
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

    public Boolean getFixedDays() {
        return fixedDays;
    }

    public void setFixedDays(Boolean fixedDays) {
        this.fixedDays = fixedDays;
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

    public Float getStopLoss() {
        return stopLoss;
    }

    public void setStopLoss(Float stopLoss) {
        this.stopLoss = stopLoss;
    }

    public Float getTakeProfit() {
        return takeProfit;
    }

    public void setTakeProfit(Float takeProfit) {
        this.takeProfit = takeProfit;
    }

    public Float getAmplitude() {
        return amplitude;
    }

    public void setAmplitude(Float amplitude) {
        this.amplitude = amplitude;
    }

    public Boolean getAmplitudePermutate() {
        return amplitudePermutate;
    }

    public void setAmplitudePermutate(Boolean amplitudePermutate) {
        this.amplitudePermutate = amplitudePermutate;
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

    public Integer getAvgDays() {
        return avgDays;
    }

    public void setAvgDays(Integer avgDays) {
        this.avgDays = avgDays;
    }

    public Boolean getAvgDaysPermutate() {
        return avgDaysPermutate;
    }

    public void setAvgDaysPermutate(Boolean avgDaysPermutate) {
        this.avgDaysPermutate = avgDaysPermutate;
    }

    public Integer getAvgDaysMin() {
        return avgDaysMin;
    }

    public void setAvgDaysMin(Integer avgDaysMin) {
        this.avgDaysMin = avgDaysMin;
    }

    public Integer getAvgDaysMax() {
        return avgDaysMax;
    }

    public void setAvgDaysMax(Integer avgDaysMax) {
        this.avgDaysMax = avgDaysMax;
    }

    public Integer getAvgDaysSteps() {
        return avgDaysSteps;
    }

    public void setAvgDaysSteps(Integer avgDaysSteps) {
        this.avgDaysSteps = avgDaysSteps;
    }

}
