package com.algos.stockscanner.strategies;

/**
 * Class to carry additional details supporting a Decision
 */
public class DecisionInfo {

    private String timestamp;

    private Float refPrice;

    private Float currPrice;

    private Float deltaAmpl;

    private Float currValue;

    private float pl;

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Float getRefPrice() {
        return refPrice;
    }

    public void setRefPrice(Float refPrice) {
        this.refPrice = refPrice;
    }

    public Float getCurrPrice() {
        return currPrice;
    }

    public void setCurrPrice(Float currPrice) {
        this.currPrice = currPrice;
    }

    public Float getDeltaAmpl() {
        return deltaAmpl;
    }

    public void setDeltaAmpl(Float deltaAmpl) {
        this.deltaAmpl = deltaAmpl;
    }

    public Float getCurrValue() {
        return currValue;
    }

    public void setCurrValue(Float currValue) {
        this.currValue = currValue;
    }

    public float getPl() {
        return pl;
    }

    public void setPl(float pl) {
        this.pl = pl;
    }


    @Override
    public String toString() {
        return "DecisionInfo{" +
                "timestamp='" + timestamp + '\'' +
                ", refPrice=" + refPrice +
                ", currPrice=" + currPrice +
                ", deltaAmpl=" + deltaAmpl +
                ", currValue=" + currValue +
                ", pl=" + pl +
                '}';
    }
}
