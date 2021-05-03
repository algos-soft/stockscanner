package com.algos.stockscanner.downloader;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.concurrent.Callable;

/**
 * Starts a background task at the scheduled time
 */
@Component
public class ScheduledTask {

    private Callable callable;
    private LocalDateTime timestamp;

    public ScheduledTask(Callable callable, LocalDateTime timestamp) {
        this.callable = callable;
        this.timestamp = timestamp;
    }

    @PostConstruct
    private void init(){

    }


}
