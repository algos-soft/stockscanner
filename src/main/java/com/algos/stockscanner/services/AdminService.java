package com.algos.stockscanner.services;

import com.algos.stockscanner.beans.Utils;
import com.algos.stockscanner.data.entity.MarketIndex;
import com.algos.stockscanner.task.TaskHandler;
import com.algos.stockscanner.task.TaskListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Service
public class AdminService {

    @Autowired
    private Utils utils;

    @Autowired
    private ApplicationContext context;

    public AdminService() {
    }

    /**
     * Download index data for a given list of indexes.
     * <p></p>
     * Each index is managed sequentially.
     * A new thread is created for each operation and we wait for it to complete before starting the next one.
     * (the web service has a max request per seconds limitation)
     *
     * We use a thread pool so that in the future we can parallelize.
     */
    public void downloadIndexData(List<MarketIndex> indexes){


        for(MarketIndex index : indexes){


        }
    }


    /**
     * Download index data for one index in a separate thread.
     * <p></p>
     * @param index the MarketIndex
     * @param mode the update mode:
     * ALL - delete all index data and load all the available data in the db
     * DATE - add/update all data starting from the given date included
     * @param startDate in case of DATE mode, the date where to begin the update od the data in the db
     * @param handler to call abort() to interrupt the process
     */
    public Future<UpdateIndexDataStatus> downloadIndexData(MarketIndex index, String mode, LocalDate startDate, TaskListener listener, TaskHandler handler){

        UpdateIndexDataCallable callable = context.getBean(UpdateIndexDataCallable.class, index, mode, startDate, listener, handler);
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        Future<UpdateIndexDataStatus> future = executorService.submit(callable);

        int i=0;
//        while(!future.isDone()){
//            try {
//                Thread.sleep(500);
//                i++;
//                System.out.println(i+": not finished");
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//        System.out.println("finished");

//        try {
//            Thread.sleep(2000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

//        try {
//            Object obj = future.get();
//            int a = 87;
//            int b=a;
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        }

        int a = 87;
        int b=a;

//            result.cancel()

        return future;
    }



    public List<UpdateIndexDataCallable> scheduleUpdate(List<MarketIndex> indexes, int intervalSeconds){
        List<UpdateIndexDataCallable> callables = new ArrayList<>();

        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(4);

        UpdateIndexDataCallable callable;
        long millis=0;
        for(MarketIndex index : indexes){
            callable = context.getBean(UpdateIndexDataCallable.class, index, "ALL", null, null, null);
            callables.add(callable);
            executorService.schedule(callable, millis, TimeUnit.MILLISECONDS);
            millis+=intervalSeconds*1000;
        }

        executorService.shutdown(); // terminate ongoing and scheduled tasks, then shutdown

        return callables;

    }


}
