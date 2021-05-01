package com.algos.stockscanner.views.indexes;

import com.algos.stockscanner.beans.Utils;
import com.algos.stockscanner.data.enums.FrequencyTypes;
import com.algos.stockscanner.data.enums.IndexCategories;
import com.vaadin.flow.component.html.Image;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;


/***
 * Model of an Index for the View
 */
public class IndexModel {

    private Integer id;
    private byte[] imageData;
    private String symbol;
    private String name;
    private IndexCategories category;
    private float spreadPercent;
    private float ovnSellDay;
    private float ovnSellWe;
    private float ovnBuyDay;
    private float ovnBuyWe;

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

    public float getSpreadPercent() {
        return spreadPercent;
    }

    public void setSpreadPercent(float spreadPercent) {
        this.spreadPercent = spreadPercent;
    }

    public float getOvnSellDay() {
        return ovnSellDay;
    }

    public void setOvnSellDay(float ovnSellDay) {
        this.ovnSellDay = ovnSellDay;
    }

    public float getOvnSellWe() {
        return ovnSellWe;
    }

    public void setOvnSellWe(float ovnSellWe) {
        this.ovnSellWe = ovnSellWe;
    }

    public float getOvnBuyDay() {
        return ovnBuyDay;
    }

    public void setOvnBuyDay(float ovnBuyDay) {
        this.ovnBuyDay = ovnBuyDay;
    }

    public float getOvnBuyWe() {
        return ovnBuyWe;
    }

    public void setOvnBuyWe(float ovnBuyWe) {
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
