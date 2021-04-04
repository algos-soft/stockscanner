package com.algos.stockscanner.beans;

import com.crazzyghost.alphavantage.AlphaVantage;
import com.crazzyghost.alphavantage.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class StartupApplicationListener implements ApplicationListener<ContextRefreshedEvent> {

    @Value("${alphavantage.api.key}")
    private String alphavantageApiKey;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {

        // Access to the API is through the AlphaVantage Singleton which is
        // accessed using the static api() method of the class.
        // Initialize the singleton with a Config instance once throughout your app's lifetime.
        Config cfg = Config.builder()
                .key(alphavantageApiKey)
                .timeOut(10)
                .build();
        AlphaVantage.api().init(cfg);

    }

}
