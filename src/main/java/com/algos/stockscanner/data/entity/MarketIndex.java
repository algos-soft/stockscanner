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
    private double BuySpreadPercent;
    private double ovnSellDay;
    private double ovnSellWe;
    private double ovnBuyDay;
    private double ovnBuyWe;

    @OneToMany(mappedBy = "index", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<IndexUnit> units = new ArrayList<>();

    private LocalDate unitsFrom;
    private LocalDate unitsTo;
    private int numUnits;
    private String unitFrequency;

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

    public double getBuySpreadPercent() {
        return BuySpreadPercent;
    }

    public void setBuySpreadPercent(double buySpreadPercent) {
        BuySpreadPercent = buySpreadPercent;
    }

    public double getOvnSellDay() {
        return ovnSellDay;
    }

    public void setOvnSellDay(double ovnSellDay) {
        this.ovnSellDay = ovnSellDay;
    }

    public double getOvnSellWe() {
        return ovnSellWe;
    }

    public void setOvnSellWe(double ovnSellWe) {
        this.ovnSellWe = ovnSellWe;
    }

    public double getOvnBuyDay() {
        return ovnBuyDay;
    }

    public void setOvnBuyDay(double ovnBuyDay) {
        this.ovnBuyDay = ovnBuyDay;
    }

    public double getOvnBuyWe() {
        return ovnBuyWe;
    }

    public void setOvnBuyWe(double ovnBuyWe) {
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

    public int getNumUnits() {
        return numUnits;
    }

    public void setNumUnits(int numUnits) {
        this.numUnits = numUnits;
    }

    public String getUnitFrequency() {
        return unitFrequency;
    }

    public void setUnitFrequency(String unitFrequency) {
        this.unitFrequency = unitFrequency;
    }
}
