package com.algos.stockscanner.downloader;

import com.algos.stockscanner.data.entity.MarketIndex;
import com.algos.stockscanner.services.UpdateIndexDataCallable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.*;

/**
 * Schedule the execution of a set of callables.
 *
 * Create a ScheduledTask for each callable, programmed for a scheduled time
 */
@Component
public class UpdateScheduler {

    private List<Callable> callables;
    private List<ScheduledTask> tasks;
    private long intervalMillis;

//    private ExecutorService executorService = Executors.newScheduledThreadPool(4);

    @Autowired
    private ApplicationContext context;

    public UpdateScheduler(List<Callable> callables, long intervalMillis) {
        this.callables = callables;
        this.intervalMillis = intervalMillis;
    }

    @PostConstruct
    private void init(){

        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(4);

        LocalDateTime timestamp = LocalDateTime.now();
        long millis=0;
        for(Callable callable : callables){
            ScheduledTask task = context.getBean(ScheduledTask.class, callable, timestamp);
            executorService.schedule(callable, millis, TimeUnit.MILLISECONDS);

            tasks.add(task);

            millis+=intervalMillis;
        }
    }

    public List<ScheduledTask> getTasks() {
        return tasks;
    }

}
