package com.algos.stockscanner.views.indexes;

import com.algos.stockscanner.data.entity.MarketIndex;
import com.vaadin.flow.component.html.Image;

public class IndexModel {

    private Integer id;
    private byte[] imageData;
    private Image image;
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

    public byte[] getImageData() {
        return imageData;
    }

    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }

//    public MarketIndex toEntity(){
//        MarketIndex entity = new MarketIndex();
//        entity.setId(getId());
//        entity.setImage(getImageData());
//        entity.setSymbol(getSymbol());
//        entity.setName(getName());
//        entity.setBuySpreadPercent(getBuySpreadPercent());
//        entity.setOvnBuyDay(getOvnBuyDay());
//        entity.setOvnBuyWe(getOvnBuyWe());
//        entity.setOvnSellDay(getOvnSellDay());
//        entity.setOvnSellWe(getOvnSellWe());
//        return entity;
//    }

    /**
     * Create a new model from an entity
     */
    public static IndexModel fromEntity(MarketIndex entity){
        IndexModel model = new IndexModel();
        model.setImageData(entity.getImage());
        model.setSymbol(entity.getSymbol());
        model.setName(entity.getName());
        model.setBuySpreadPercent(entity.getBuySpreadPercent());
        model.setOvnBuyDay(entity.getOvnBuyDay());
        model.setOvnBuyWe(entity.getOvnBuyWe());
        model.setOvnSellDay(entity.getOvnSellDay());
        model.setOvnSellWe(entity.getOvnSellWe());
        return model;
    }

}
