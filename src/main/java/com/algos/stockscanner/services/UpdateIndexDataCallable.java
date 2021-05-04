package com.algos.stockscanner.services;

import com.algos.stockscanner.data.entity.MarketIndex;
import com.algos.stockscanner.task.TaskHandler;
import com.algos.stockscanner.task.TaskListener;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.concurrent.Callable;

@Component
@Scope("prototype")
public class UpdateIndexDataCallable implements Callable<UpdateIndexDataStatus> {

    private MarketIndex index;
    private String mode;
    private LocalDate startDate;
    private TaskListener listener;
    private TaskHandler handler;

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

    @Override
    public UpdateIndexDataStatus call() throws Exception {

        System.out.println("Callable task called for index "+index.getSymbol());

        // long task, can throw exception
        try {

            int tot=100;
            for(int i=0;i<tot;i++){

                if(i==80){
                    throw new Exception("Internal error!");
                }

                if(listener!=null){
                    listener.onProgress(i, tot, null);
                }

                if(handler!=null){
                    if(handler.isAborted()){
                        listener.onCompleted(false);
                        return null;
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

        if(listener!=null){
            System.out.println("Callable task completed for index "+index.getSymbol());
            listener.onCompleted(true);
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
