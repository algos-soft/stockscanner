package com.algos.stockscanner.strategies;

/**
 * Class to carry additional details supporting a Decision
 */
public class DecisionInfo {

    private String timestamp;

    private Float refPrice;

    private Float currPrice;

    private Float deltaAmpl;

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


    @Override
    public String toString() {
        return "DecisionInfo{" +
                "timestamp='" + timestamp + '\'' +
                ", refPrice=" + refPrice +
                ", currPrice=" + currPrice +
                ", deltaAmpl=" + deltaAmpl +
                '}';
    }
}
