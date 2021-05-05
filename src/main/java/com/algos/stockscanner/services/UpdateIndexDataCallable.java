package com.algos.stockscanner.services;

import com.algos.stockscanner.data.entity.MarketIndex;
import com.algos.stockscanner.task.TaskHandler;
import com.algos.stockscanner.task.TaskListener;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Callable;

@Component
@Scope("prototype")
public class UpdateIndexDataCallable implements Callable<UpdateIndexDataStatus> {

    private MarketIndex index;
    private String mode;
    private LocalDate startDate;
    private TaskListener listener;
    private TaskHandler handler;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    /**
     * @param index the index to update
     * @param mode the update mode
     * ALL - delete all index data and load all the available data in the db
     * DATE - add/update all data starting from the given date included
     * @param startDate in case of DATE mode, the date when to begin the update
     * @param listener listener to inform with progress events (optional)
     * @param handler to check for external abort requests (optional)
     */
    public UpdateIndexDataCallable(MarketIndex index, String mode, LocalDate startDate, TaskListener listener, TaskHandler handler) {
        this.index=index;
        this.mode=mode;
        this.startDate=startDate;
        this.listener = listener;
        this.handler = handler;
    }

    @PostConstruct
    private void init(){
    }

    @Override
    public UpdateIndexDataStatus call() throws Exception {

        System.out.println("Callable task called for index "+index.getSymbol());
        startTime=LocalDateTime.now();

        // long task, can throw exception
        try {

            int tot=100;
            for(int i=0;i<tot;i++){

                if(listener!=null){
                    listener.onProgress(i, tot, index.getSymbol());
                }

                if(handler!=null){
                    if(handler.isAborted()){
                        throw new Exception("Aborted by user");
                    }
                }

                Thread.sleep(100);
            }

        }catch (Exception e){

            System.out.println("Callable task error for index "+index.getSymbol()+": "+e.getMessage());


            if(listener!=null){
                listener.onError(e);
            }
            return null;
        }

        endTime=LocalDateTime.now();


        if(listener!=null){
            System.out.println("Callable task completed for index "+index.getSymbol());
            Duration duration=Duration.between(startTime, endTime);
            String sDuration = DurationFormatUtils.formatDuration(duration.toMillis(), "H:mm:ss", true);
            DateTimeFormatter format = DateTimeFormatter.ofPattern("HH:mm:ss");
            String info = "start: "+startTime.format(format)+", end: "+endTime.format(format)+", elapsed: "+sDuration;
            listener.onCompleted(info);
        }

        return new UpdateIndexDataStatus();

    }


    public TaskListener getListener() {
        return listener;
    }

    public void setListener(TaskListener listener) {
        this.listener = listener;
    }

    public TaskHandler getHandler() {
        return handler;
    }

    public void setHandler(TaskHandler handler) {
        this.handler = handler;
    }
}
