package com.algos.stockscanner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.vaadin.artur.helpers.LaunchUtil;

/**
 * The entry point of the Spring Boot application.
 */
@SpringBootApplication
public class Application extends SpringBootServletInitializer {

    // the resource for the generic index icon
    public static final String GENERIC_INDEX_ICON="images/generic_index.jpg";

    // size of a stored icon on the database (the icon is always resized before storage)
    public static final int STORED_ICON_WIDTH=128;
    public static final int STORED_ICON_HEIGHT =128;

    public static void main(String[] args) {
        LaunchUtil.launchBrowserInDevelopmentMode(SpringApplication.run(Application.class, args));
    }

}
