package com.algos.stockscanner.data.entity;

import javax.persistence.*;

import com.algos.stockscanner.data.AbstractEntity;
import com.algos.stockscanner.utils.Du;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Simulation extends AbstractEntity {

    // --- parameters
    @ManyToOne(fetch = FetchType.EAGER)
    private MarketIndex index;

    @ManyToOne(fetch = FetchType.EAGER)
    private Generator generator;

    @OneToMany(mappedBy = "simulation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SimulationItem> simulationItems=new ArrayList<>();

    private String startTs;  // timestamp of first point scanned
    private String endTs;  // timestamp of last point scanned
    private Float initialAmount;  // initial amount
    private Integer leverage;
    private Float sl;
    private Float tp;
    private Float amplitude;    // amplitude of the oscillation max/min, in percent
    private Integer daysLookback;    // number of days to lookback to determine the avg price

    // ---- consolidated data
    private Float finalAmount;  // amount at the end of the simulation
    private String terminationCode; // why the simulation is terminated
    private Float totSpread;    // total amount of buy spreads paid
    private Float totCommission;    // total amount of commission paid
    private Integer numBuy;    // number of buy positions opened
    private Integer numSell;    // number of sell positions opened
    private Float pl;  // profit/loss
    private Float plPercent;  // profit/loss percentage
    private Integer numPointsScanned; // number of points scanned
    private Integer numPointsHold;  // total number of points while holding a position
    private Integer numPointsWait;  // total number of points points while not holding a position
    private Integer minPointsHold;  // minimum number of consecutive points while holding a position
    private Integer maxPointsHold;    // maximum number of consecutive points while holding a position

    public MarketIndex getIndex() {
        return index;
    }

    public void setIndex(MarketIndex index) {
        this.index = index;
    }

    public Generator getGenerator() {
        return generator;
    }

    public void setGenerator(Generator generator) {
        this.generator = generator;
    }

    public List<SimulationItem> getSimulationItems() {
        return simulationItems;
    }

    public void setSimulationItems(List<SimulationItem> simulationItems) {
        this.simulationItems = simulationItems;
    }

    public String getStartTs() {
        return startTs;
    }

    public void setStartTs(String startTs) {
        this.startTs = startTs;
    }

    public String getEndTs() {
        return endTs;
    }

    public void setEndTs(String endTs) {
        this.endTs = endTs;
    }

    public Float getInitialAmount() {
        return initialAmount;
    }

    public void setInitialAmount(Float initialAmount) {
        this.initialAmount = initialAmount;
    }

    public Integer getLeverage() {
        return leverage;
    }

    public void setLeverage(Integer leverage) {
        this.leverage = leverage;
    }

    public Float getSl() {
        return sl;
    }

    public void setSl(Float sl) {
        this.sl = sl;
    }

    public Float getTp() {
        return tp;
    }

    public void setTp(Float tp) {
        this.tp = tp;
    }

    public Float getAmplitude() {
        return amplitude;
    }

    public void setAmplitude(Float amplitude) {
        this.amplitude = amplitude;
    }


    public Integer getDaysLookback() {
        return daysLookback;
    }

    public void setDaysLookback(Integer daysLookback) {
        this.daysLookback = daysLookback;
    }

    public Float getFinalAmount() {
        return finalAmount;
    }

    public void setFinalAmount(Float finalAmount) {
        this.finalAmount = finalAmount;
    }

    public String getTerminationCode() {
        return terminationCode;
    }

    public void setTerminationCode(String terminationCode) {
        this.terminationCode = terminationCode;
    }

    public Float getTotSpread() {
        return totSpread;
    }

    public void setTotSpread(Float totSpread) {
        this.totSpread = totSpread;
    }

    public Float getTotCommission() {
        return totCommission;
    }

    public void setTotCommission(Float totCommission) {
        this.totCommission = totCommission;
    }

    public Integer getNumBuy() {
        return numBuy;
    }

    public void setNumBuy(Integer numBuy) {
        this.numBuy = numBuy;
    }

    public Integer getNumSell() {
        return numSell;
    }

    public void setNumSell(Integer numSell) {
        this.numSell = numSell;
    }

    public Float getPl() {
        return pl;
    }

    public void setPl(Float pl) {
        this.pl = pl;
    }

    public Float getPlPercent() {
        return plPercent;
    }

    public void setPlPercent(Float plPercent) {
        this.plPercent = plPercent;
    }

    public Integer getNumPointsScanned() {
        return numPointsScanned;
    }

    public void setNumPointsScanned(Integer numPointsScanned) {
        this.numPointsScanned = numPointsScanned;
    }

    public Integer getNumPointsHold() {
        return numPointsHold;
    }

    public void setNumPointsHold(Integer numPointsHold) {
        this.numPointsHold = numPointsHold;
    }

    public Integer getNumPointsWait() {
        return numPointsWait;
    }

    public void setNumPointsWait(Integer numPointsWait) {
        this.numPointsWait = numPointsWait;
    }

    public Integer getMinPointsHold() {
        return minPointsHold;
    }

    public void setMinPointsHold(Integer minPointsHold) {
        this.minPointsHold = minPointsHold;
    }

    public Integer getMaxPointsHold() {
        return maxPointsHold;
    }

    public void setMaxPointsHold(Integer maxPointsHold) {
        this.maxPointsHold = maxPointsHold;
    }

    // ----------------

    public LocalDateTime getStartTsLDT() {
        return Du.toLocalDateTime(startTs);
    }

    public LocalDate getStartTsLD() {
        return Du.toLocalDate(getStartTsLDT());
    }

    public void setStartTsLDT(LocalDateTime startTs) {
        this.startTs=Du.toUtcString(startTs);
    }

    public LocalDateTime getEndTsLDT() {
        return Du.toLocalDateTime(endTs);
    }

    public LocalDate getEndTsLD() {
        return Du.toLocalDate(getEndTsLDT());
    }

    public void setEndTsLDT(LocalDateTime endTs) {
        this.endTs=Du.toUtcString(endTs);
    }


}
