package com.algos.stockscanner.services;

import com.algos.stockscanner.beans.ContextStore;
import com.algos.stockscanner.data.entity.MarketIndex;
import com.algos.stockscanner.task.TaskHandler;
import com.algos.stockscanner.task.TaskListener;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
@Scope("prototype")
public class UpdateIndexDataCallable implements Callable<Void> {

    private static final Logger log = LoggerFactory.getLogger(UpdateIndexDataCallable.class);

    private MarketIndex index;
    private String mode;
    private LocalDate startDate;
    private ConcurrentLinkedQueue<TaskListener> listeners = new ConcurrentLinkedQueue<>();
    private ConcurrentLinkedQueue<TaskHandler> handlers = new ConcurrentLinkedQueue<>();
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @Autowired
    private ContextStore contextStore;

    /**
     * @param index     the index to update
     * @param mode      the update mode
     *                  ALL - delete all index data and load all the available data in the db
     *                  DATE - add/update all data starting from the given date included
     * @param startDate in case of DATE mode, the date when to begin the update
     */
    public UpdateIndexDataCallable(MarketIndex index, String mode, LocalDate startDate) {
        this.index = index;
        this.mode = mode;
        this.startDate = startDate;
    }

    @PostConstruct
    private void init() {
    }

    @Override
    public Void call() {

        log.debug("Callable task called for index " + index.getSymbol());

        // register itself to the context-level storage
        contextStore.updateIndexCallableMap.put("" + hashCode(), this);

        startTime = LocalDateTime.now();

        // long task, can throw exception
        try {

            int tot = 100;
            for (int i = 0; i < tot; i++) {

                notifyProgress(i, tot, index.getSymbol());

                checkHandlers();    // throws exception if at least one handler is aborted

                Thread.sleep(100);

            }

            endTime = LocalDateTime.now();
            String info = buildDurationInfo();
            log.debug("Callable task completed for index " + index.getSymbol()+" "+info);
            notifyCompleted(info);

        } catch (Exception e) {

            endTime = LocalDateTime.now();
            log.error("Callable task error for index " + index.getSymbol(), e);
            notifyError(e);

        }

        // unregister itself from the context-level storage
        contextStore.updateIndexCallableMap.remove("" + hashCode(), this);

        return null;

    }


    private String buildDurationInfo(){
        Duration duration = Duration.between(startTime, endTime);
        String sDuration = DurationFormatUtils.formatDuration(duration.toMillis(), "H:mm:ss", true);
        DateTimeFormatter format = DateTimeFormatter.ofPattern("HH:mm:ss");
        String info = "start: " + startTime.format(format) + ", end: " + endTime.format(format) + ", elapsed: " + sDuration;
        return info;
    }

    /**
     * @param listener listener to inform with progress events
     */
    public void addListener(TaskListener listener) {
        this.listeners.add(listener);
    }

    /**
     * @param handler handler to check for external abort requests (optional)
     */
    public void addHandler(TaskHandler handler) {
        this.handlers.add(handler);
    }

    private void notifyProgress(int current, int tot, String info){
        for(TaskListener listener : listeners){
            listener.onProgress(current, tot, info);
        }
    }

    private void notifyError(Exception e){
        for(TaskListener listener : listeners){
            listener.onError(e);
        }
    }

    private void notifyCompleted(String info){
        for(TaskListener listener : listeners){
            listener.onCompleted(info);
        }
    }

    private void checkHandlers() throws Exception{
        for(TaskHandler handler : handlers){
            if (handler.isAborted()) {
                throw new Exception("Aborted by user");
            }
        }
    }




}
