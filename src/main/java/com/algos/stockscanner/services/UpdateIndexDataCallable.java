package com.algos.stockscanner.services;

import com.algos.stockscanner.data.entity.MarketIndex;
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
    private UpdateIndexDataListener listener;
    private UpdateIndexDataHandler handler;

    /**
     * @param index the index to update
     * @param mode the update mode
     * ALL - delete all index data and load all the available data in the db
     * DATE - add/update all data starting from the given date included
     * @param startDate in case of DATE mode, the date when to begin the update
     * @param listener listener to inform with progress events (optional)
     * @param handler to check for external abort requests (optional)
     */
    public UpdateIndexDataCallable(MarketIndex index, String mode, LocalDate startDate, UpdateIndexDataListener listener, UpdateIndexDataHandler handler) {
        this.index=index;
        this.mode=mode;
        this.startDate=startDate;
        this.listener = listener;
        this.handler=handler;
    }

    @Override
    public UpdateIndexDataStatus call() throws Exception {

        boolean aborted=false;

        // 10 secondi
        int tot=100;
        for(int i=0;i<tot;i++){

            if(listener!=null){
                listener.onProgress(i, tot, null);
            }

            if(handler!=null){
                if(handler.isAborted()){
                    aborted=true;
                    break;
                }
            }

            Thread.sleep(100);
        }

        listener.onCompleted(aborted);

        return new UpdateIndexDataStatus();

    }
}
