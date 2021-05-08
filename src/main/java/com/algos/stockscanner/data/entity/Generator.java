package com.algos.stockscanner.data.entity;

import com.algos.stockscanner.data.AbstractEntity;
import com.algos.stockscanner.utils.Du;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Generator extends AbstractEntity {

    // remember: never use primitives or initialize values in JPA entities
    // if you want to use Query by Example. JPA relies on nulls in query by example

    @ManyToOne(fetch = FetchType.EAGER)
    private MarketIndex index;

    @OneToMany(mappedBy = "generator", fetch=FetchType.EAGER, cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Simulation> simulations=new ArrayList<>();

    @ManyToMany
    @LazyCollection(LazyCollectionOption.FALSE)
    @JoinTable(name = "generator_index")
    private List<MarketIndex> indexes=new ArrayList<>();

    private LocalDateTime created;
    private LocalDateTime modified;

    // fixed properties
    private Integer number;   // human readable number of the Configurator
    private String name;  // mnemonic name
    private String startDate;  // start date
    private Boolean fixedDays;  // true for fixed number of days, false to go or until TP/SL or until end of index data
    private Integer days;   // number of days
    private Integer spans;   // number of times to repeat the simulation, starting from the end of the previous simulation
    private Integer amount;  // amount invested
    private Integer stopLoss; // applied to each open/close cycle inside a span, in percent
    private Integer takeProfit;  // applied to each open/close cycle inside a span, in percent

    // permutable properties

    private Integer amplitude;  // the difference in percent that can triggers a buy or a sell
    private Boolean amplitudePermutate;
    private Integer amplitudeMin;    // min amplitude, percent
    private Integer amplitudeMax;    // max amplitude, percent
    private Integer amplitudeSteps;    // how many steps

    private Integer avgDays;    // number of days taken in account to calculate the starting average value
    private Boolean avgDaysPermutate;
    private Integer avgDaysMin;
    private Integer avgDaysMax;
    private Integer avgDaysSteps;   // must be divisor of maxAvgDays-minAvgDays

    private Boolean indexesPermutate;

    public List<Simulation> getSimulations() {
        return simulations;
    }

    public void setSimulations(List<Simulation> simulations) {
        this.simulations = simulations;
    }

    public void setIndexes(List<MarketIndex> indexes) {
        this.indexes = indexes;
    }

    public List<MarketIndex> getIndexes() {
        return indexes;
//        return new ArrayList<MarketIndex>();
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
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

    public Integer getSpans() {
        return spans;
    }

    public void setSpans(Integer spans) {
        this.spans = spans;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public Integer getStopLoss() {
        return stopLoss;
    }

    public void setStopLoss(Integer stopLoss) {
        this.stopLoss = stopLoss;
    }

    public Integer getTakeProfit() {
        return takeProfit;
    }

    public void setTakeProfit(Integer takeProfit) {
        this.takeProfit = takeProfit;
    }

    public Integer getAmplitude() {
        return amplitude;
    }

    public void setAmplitude(Integer amplitude) {
        this.amplitude = amplitude;
    }

    public Boolean getAmplitudePermutate() {
        return amplitudePermutate;
    }

    public void setAmplitudePermutate(Boolean amplitudePermutate) {
        this.amplitudePermutate = amplitudePermutate;
    }

    public Integer getAmplitudeMin() {
        return amplitudeMin;
    }

    public void setAmplitudeMin(Integer amplitudeMin) {
        this.amplitudeMin = amplitudeMin;
    }

    public Integer getAmplitudeMax() {
        return amplitudeMax;
    }

    public void setAmplitudeMax(Integer amplitudeMax) {
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

    public Boolean getIndexesPermutate() {
        return indexesPermutate;
    }

    public void setIndexesPermutate(Boolean indexesPermutate) {
        this.indexesPermutate = indexesPermutate;
    }

    // --------------
    public LocalDate getStartDateLD(){
        return Du.toLocalDate(startDate);
    }

    public void setStartDateLD(LocalDate localDate) {
        this.startDate = Du.toUtcString(localDate);
    }

    @Override
    public String toString() {
        return "Generator{" +
                "index=" + index +
                ", simulations=" + simulations +
                ", indexes=" + indexes +
                ", created=" + created +
                ", modified=" + modified +
                ", number=" + number +
                ", startDate='" + startDate + '\'' +
                ", fixedDays=" + fixedDays +
                ", days=" + days +
                ", spans=" + spans +
                ", amount=" + amount +
                ", stopLoss=" + stopLoss +
                ", takeProfit=" + takeProfit +
                ", amplitude=" + amplitude +
                ", amplitudePermutate=" + amplitudePermutate +
                ", amplitudeMin=" + amplitudeMin +
                ", amplitudeMax=" + amplitudeMax +
                ", amplitudeSteps=" + amplitudeSteps +
                ", avgDays=" + avgDays +
                ", avgDaysPermutate=" + avgDaysPermutate +
                ", avgDaysMin=" + avgDaysMin +
                ", avgDaysMax=" + avgDaysMax +
                ", avgDaysSteps=" + avgDaysSteps +
                ", indexesPermutate=" + indexesPermutate +
                '}';
    }
}
