package com.algos.stockscanner.data.entity;

import com.algos.stockscanner.data.AbstractEntity;
import com.algos.stockscanner.utils.Du;

import javax.persistence.*;
import java.time.*;

@Entity
@Table(indexes = @Index(columnList = "dateTime"))
public class IndexUnit extends AbstractEntity {

    private Float open;
    private Float close;
    private String dateTime;

    @ManyToOne(fetch=FetchType.EAGER)
    private MarketIndex index;

    public Float getOpen() {
        return open;
    }

    public void setOpen(Float open) {
        this.open = open;
    }

    public Float getClose() {
        return close;
    }

    public void setClose(Float close) {
        this.close = close;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public MarketIndex getIndex() {
        return index;
    }

    public void setIndex(MarketIndex index) {
        this.index = index;
    }


    // --------------

    public LocalDateTime getDateTimeLDT() {
        return Du.toLocalDateTime(dateTime);
    }

    public void setDateTimeLDT(LocalDateTime dateTime) {
        this.dateTime=Du.toUtcString(dateTime);
    }

}
