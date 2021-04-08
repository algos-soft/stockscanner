package com.algos.stockscanner.data.entity;

import com.algos.stockscanner.data.AbstractEntity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(indexes = @Index(columnList = "dateTime"))
public class IndexUnit extends AbstractEntity {

    private Float open;
    private Float close;
    private LocalDateTime dateTime;

    @ManyToOne(fetch = FetchType.LAZY)
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

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public MarketIndex getIndex() {
        return index;
    }

    public void setIndex(MarketIndex index) {
        this.index = index;
    }
}
