package com.algos.stockscanner.data.entity;

import com.algos.stockscanner.data.AbstractEntity;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
public class MarketIndex extends AbstractEntity {


    @Column(length=65535) // -> MySQL BLOB type
    private byte[] image;
    private String symbol;
    private String name;
    private String category;
    private Double buySpreadPercent;
    private Double ovnSellDay;
    private Double ovnSellWe;
    private Double ovnBuyDay;
    private Double ovnBuyWe;


    private LocalDate unitsFrom;
    private LocalDate unitsTo;
    private Integer numUnits;

    private String unitFrequency;

    @OneToMany(mappedBy = "index", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<IndexUnit> units = new ArrayList<>();

    @OneToMany(mappedBy = "index", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Generator> generators = new ArrayList<>();

    @OneToMany(mappedBy = "index", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Simulation> simulations = new ArrayList<>();


    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Double getBuySpreadPercent() {
        return buySpreadPercent;
    }

    public void setBuySpreadPercent(Double buySpreadPercent) {
        this.buySpreadPercent = buySpreadPercent;
    }

    public Double getOvnSellDay() {
        return ovnSellDay;
    }

    public void setOvnSellDay(Double ovnSellDay) {
        this.ovnSellDay = ovnSellDay;
    }

    public Double getOvnSellWe() {
        return ovnSellWe;
    }

    public void setOvnSellWe(Double ovnSellWe) {
        this.ovnSellWe = ovnSellWe;
    }

    public Double getOvnBuyDay() {
        return ovnBuyDay;
    }

    public void setOvnBuyDay(Double ovnBuyDay) {
        this.ovnBuyDay = ovnBuyDay;
    }

    public Double getOvnBuyWe() {
        return ovnBuyWe;
    }

    public void setOvnBuyWe(Double ovnBuyWe) {
        this.ovnBuyWe = ovnBuyWe;
    }

    public List<IndexUnit> getUnits() {
        return units;
    }

    public void setUnits(List<IndexUnit> units) {
        this.units = units;
    }

    public LocalDate getUnitsFrom() {
        return unitsFrom;
    }

    public void setUnitsFrom(LocalDate unitsFrom) {
        this.unitsFrom = unitsFrom;
    }

    public LocalDate getUnitsTo() {
        return unitsTo;
    }

    public void setUnitsTo(LocalDate unitsTo) {
        this.unitsTo = unitsTo;
    }

    public Integer getNumUnits() {
        return numUnits;
    }

    public void setNumUnits(Integer numUnits) {
        this.numUnits = numUnits;
    }

    public String getUnitFrequency() {
        return unitFrequency;
    }

    public void setUnitFrequency(String unitFrequency) {
        this.unitFrequency = unitFrequency;
    }
}
