package com.algos.stockscanner.beans;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableAsync
public class Scheduler {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Value("${index.update.enable}")
    private boolean indexUpdateEnable;

    @Value("${price.update.enable}")
    private boolean priceUpdateEnable;

    @Async
    @Scheduled(cron = "${index.update.cron}")
    public void scheduleIndexUpdates(){
        if(indexUpdateEnable){
            log.info("index update scheduler triggered");
        }
    }

    @Async
    @Scheduled(cron = "${price.update.cron}")
    public void schedulePriceUpdates(){
        if(priceUpdateEnable){
            log.info("price update scheduler triggered");
        }
    }


}
