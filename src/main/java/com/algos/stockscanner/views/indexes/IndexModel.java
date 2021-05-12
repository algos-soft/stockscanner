package com.algos.stockscanner.views.indexes;

import com.algos.stockscanner.enums.FrequencyTypes;
import com.algos.stockscanner.enums.IndexCategories;

import java.time.LocalDate;
import java.time.LocalDateTime;


/***
 * Model of an Index for the View
 */
public class IndexModel {

    private Integer id;
    private byte[] imageData;
    private String symbol;
    private String name;
    private IndexCategories category;

    private String exchange;
    private String country;
    private String sector;
    private String industry;
    private long marketCap;
    private long ebitda;

    private float spreadPercent;
    private float ovnSellDay;
    private float ovnSellWe;
    private float ovnBuyDay;
    private float ovnBuyWe;

    private LocalDate unitsFrom;
    private LocalDate unitsTo;
    private int numUnits;
    private FrequencyTypes unitFrequency;

    private LocalDateTime fundamentalUpdateTs;
    private LocalDateTime pricesUpdateTs;


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

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getSector() {
        return sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public long getMarketCap() {
        return marketCap;
    }

    public void setMarketCap(long marketCap) {
        this.marketCap = marketCap;
    }

    public long getEbitda() {
        return ebitda;
    }

    public void setEbitda(long ebitda) {
        this.ebitda = ebitda;
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

    public LocalDateTime getFundamentalUpdateTs() {
        return fundamentalUpdateTs;
    }

    public void setFundamentalUpdateTs(LocalDateTime fundamentalUpdateTs) {
        this.fundamentalUpdateTs = fundamentalUpdateTs;
    }

    public LocalDateTime getPricesUpdateTs() {
        return pricesUpdateTs;
    }

    public void setPricesUpdateTs(LocalDateTime pricesUpdateTs) {
        this.pricesUpdateTs = pricesUpdateTs;
    }

}
