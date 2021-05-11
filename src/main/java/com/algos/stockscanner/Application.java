package com.algos.stockscanner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.vaadin.artur.helpers.LaunchUtil;

import javax.annotation.PostConstruct;
import java.util.TimeZone;

/**
 * The entry point of the Spring Boot application.
 */
@SpringBootApplication
@EnableScheduling
public class Application extends SpringBootServletInitializer {

    public static final String APP_NAME="StockScanner";

    // the resource for the generic index icon
    public static final String GENERIC_INDEX_ICON="images/generic_index.jpg";

    // the file containing all the potentially available symbols
    public static final String ALL_AVAILABLE_SYMBOLS="config/indexes.csv";


    // size of a stored icon on the database (the icon is always resized before storage)
    public static final int STORED_ICON_WIDTH=128;
    public static final int STORED_ICON_HEIGHT =128;

    @PostConstruct
    void started() {
        // set JVM timezone as UTC
        //TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    public static void main(String[] args) {
        LaunchUtil.launchBrowserInDevelopmentMode(SpringApplication.run(Application.class, args));
    }



}
