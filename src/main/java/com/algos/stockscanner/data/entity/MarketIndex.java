package com.algos.stockscanner.data.entity;

import com.algos.stockscanner.data.AbstractEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.time.LocalDate;

@Entity
public class MarketIndex extends AbstractEntity {


    @Column(length=65535) // -> MySQL BLOB type
    private byte[] image;
    private String symbol;
    private String name;
    private double BuySpreadPercent;
    private double ovnSellDay;
    private double ovnSellWe;
    private double ovnBuyDay;
    private double ovnBuyWe;

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
}
