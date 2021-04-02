package com.algos.stockscanner.views.indexes;

import com.algos.stockscanner.data.entity.MarketIndex;
import org.apache.commons.lang3.ObjectUtils;

public class IndexModel {

    private String image;
    private String symbol;
    private String name;
    private double BuySpreadPercent;
    private double ovnSellDay;
    private double ovnSellWe;
    private double ovnBuyDay;
    private double ovnBuyWe;



    private String date;
    private String post;
    private String likes;
    private String comments;
    private String shares;

    public IndexModel() {
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }






    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPost() {
        return post;
    }

    public void setPost(String post) {
        this.post = post;
    }

    public String getLikes() {
        return likes;
    }

    public void setLikes(String likes) {
        this.likes = likes;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getShares() {
        return shares;
    }

    public void setShares(String shares) {
        this.shares = shares;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
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


    public MarketIndex toEntity(){
        MarketIndex index = new MarketIndex();
        index.setSymbol(getSymbol());
        index.setName(getName());
        index.setImage(null);
        index.setBuySpreadPercent(getBuySpreadPercent());
        index.setOvnBuyDay(getOvnBuyDay());
        index.setOvnBuyWe(getOvnBuyWe());
        index.setOvnSellDay(getOvnSellDay());
        index.setOvnSellWe(getOvnSellWe());
        return index;
    }
}
