package com.algos.stockscanner.utils;

import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Du {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss");


    public static LocalDate toLocalDate(String utcString) {
        if(StringUtils.isEmpty(utcString)){
            return null;
        }
        return LocalDate.parse(utcString, DATE_FORMATTER);
    }

    public static String toUtcString(LocalDate localDate) {
        if(localDate==null){
            return null;
        }
        return localDate.format(DATE_FORMATTER);
    }

    public static LocalDateTime toLocalDateTime(String utcString) {
        if(StringUtils.isEmpty(utcString)){
            return null;
        }
        return LocalDateTime.parse(utcString, DATE_TIME_FORMATTER);
    }

    public static String toUtcString(LocalDateTime localDateTime) {
        if(localDateTime==null){
            return null;
        }
        return localDateTime.format(DATE_TIME_FORMATTER);
    }



}
