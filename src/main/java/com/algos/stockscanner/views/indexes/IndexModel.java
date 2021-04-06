package com.algos.stockscanner.views.indexes;

import com.algos.stockscanner.data.entity.FrequencyTypes;
import com.algos.stockscanner.data.entity.IndexCategories;
import com.algos.stockscanner.data.entity.MarketIndex;
import com.vaadin.flow.component.html.Image;

import java.time.LocalDate;


/***
 * Model of an Index for the View
 */
public class IndexModel {

    private Integer id;
    private byte[] imageData;
    private Image image;
    private String symbol;
    private String name;
    private IndexCategories category;
    private double BuySpreadPercent;
    private double ovnSellDay;
    private double ovnSellWe;
    private double ovnBuyDay;
    private double ovnBuyWe;

    private LocalDate unitsFrom;
    private LocalDate unitsTo;
    private int numUnits;
    private FrequencyTypes unitFrequency;

    public IndexModel() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public IndexCategories getCategory() {
        return category;
    }

    public void setCategory(IndexCategories category) {
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

    public byte[] getImageData() {
        return imageData;
    }

    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
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

    public FrequencyTypes getUnitFrequency() {
        return unitFrequency;
    }

    public void setUnitFrequency(FrequencyTypes unitFrequency) {
        this.unitFrequency = unitFrequency;
    }
}
