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
    private Integer sl;
    private Integer tp;
    private Float amplitude;    // amplitude of the oscillation max/min, in percent
    private Integer daysLookback;    // number of days to lookback to determine the avg price

    // ---- consolidated data
    private String terminationCode; // why the simulation is terminated
    private Float totSpread;    // total amount of buy spreads paid
    private Float totCommission;    // total amount of commission paid
    private Float pl;  // profit/loss
    private Float plPercent;  // profit/loss percentage
    private Integer numPointsTotal; // total number of points scanned
    private Integer numPointsOpen;  // number of points with position open
    private Integer numPointsClosed;  // number of points with position closed
    private Integer shortestPeriodOpen;  // minimum number of consecutive points with position open
    private Integer longestPeriodOpen;    // maximum number of consecutive points with position open

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

    public Integer getSl() {
        return sl;
    }

    public void setSl(Integer sl) {
        this.sl = sl;
    }

    public Integer getTp() {
        return tp;
    }

    public void setTp(Integer tp) {
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

    public Integer getNumPointsTotal() {
        return numPointsTotal;
    }

    public void setNumPointsTotal(Integer numPointsTotal) {
        this.numPointsTotal = numPointsTotal;
    }

    public Integer getNumPointsOpen() {
        return numPointsOpen;
    }

    public void setNumPointsOpen(Integer numPointsOpen) {
        this.numPointsOpen = numPointsOpen;
    }

    public Integer getNumPointsClosed() {
        return numPointsClosed;
    }

    public void setNumPointsClosed(Integer numPointsClosed) {
        this.numPointsClosed = numPointsClosed;
    }

    public Integer getShortestPeriodOpen() {
        return shortestPeriodOpen;
    }

    public void setShortestPeriodOpen(Integer shortestPeriodOpen) {
        this.shortestPeriodOpen = shortestPeriodOpen;
    }

    public Integer getLongestPeriodOpen() {
        return longestPeriodOpen;
    }

    public void setLongestPeriodOpen(Integer longestPeriodOpen) {
        this.longestPeriodOpen = longestPeriodOpen;
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
