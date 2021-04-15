package com.algos.stockscanner.data.entity;

import com.algos.stockscanner.data.AbstractEntity;

import javax.persistence.*;
import java.time.*;

@Entity
@Table(indexes = @Index(columnList = "dateTime"))
public class IndexUnit extends AbstractEntity {

    private Float open;
    private Float close;

//    @Column(columnDefinition = "TIMESTAMP")
//    private LocalDateTime dateTime;

    @Column(columnDefinition = "DATETIME")
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
//        ZonedDateTime ldtZoned = dateTime.atZone(ZoneId.systemDefault());
//        ZonedDateTime utcZoned = ldtZoned.withZoneSameInstant(ZoneId.of("UTC"));
//        LocalDateTime ldt = utcZoned.toLocalDateTime();
//        return ldt;
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

//    public LocalDateTime getLocalDateTime() {
////        ZonedDateTime ldtZoned = dateTime.atZone(ZoneId.systemDefault());
////        ZonedDateTime utcZoned = ldtZoned.withZoneSameInstant(ZoneId.of("UTC"));
////        LocalDateTime ldt = utcZoned.toLocalDateTime();
////        return ldt;
//        return dateTime.toLocalDateTime();
//    }

//    public void setLocalDateTime(LocalDateTime dateTime) {
//        final ZoneId zone = ZoneId.systemDefault();
//        ZoneOffset zoneOffSet = zone.getRules().getOffset(dateTime);
//        OffsetDateTime offsetDateTime = dateTime.atOffset(zoneOffSet);
//        this.dateTime = offsetDateTime;
//    }



    public MarketIndex getIndex() {
        return index;
    }

    public void setIndex(MarketIndex index) {
        this.index = index;
    }
}
