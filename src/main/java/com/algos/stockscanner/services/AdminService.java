package com.algos.stockscanner.services;

import com.algos.stockscanner.beans.ContextStore;
import com.algos.stockscanner.beans.Utils;
import com.algos.stockscanner.data.entity.MarketIndex;
import com.algos.stockscanner.enums.IndexDownloadModes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Service
public class AdminService {

    @Autowired
    private Utils utils;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private ContextStore contextStore;

    private ScheduledExecutorService executorService;


    public AdminService() {
    }

    @PostConstruct
    private void init(){
        executorService = Executors.newScheduledThreadPool(Integer.MAX_VALUE);
    }

    public List<UpdateIndexDataCallable> scheduleUpdate(List<MarketIndex> indexes, int intervalSeconds){
        List<UpdateIndexDataCallable> callables = new ArrayList<>();


        UpdateIndexDataCallable callable;
        long millis=0;
        for(MarketIndex index : indexes){
            callable = context.getBean(UpdateIndexDataCallable.class, index, "ALL", null);
            callables.add(callable);
            executorService.schedule(callable, millis, TimeUnit.MILLISECONDS);
            millis+=intervalSeconds*1000;
        }

        //executorService.shutdown(); // terminate ongoing and scheduled tasks, then shutdown

        return callables;

    }


    public List<DownloadIndexCallable> scheduleDownload(List<String> symbols, int intervalSeconds){
        List<DownloadIndexCallable> callables = new ArrayList<>();

        DownloadIndexCallable callable;
        long millis=0;
        for(String symbol : symbols){
            callable = context.getBean(DownloadIndexCallable.class, symbol);
            callables.add(callable);
            executorService.schedule(callable, millis, TimeUnit.MILLISECONDS);
            millis+=intervalSeconds*1000;
        }

        return callables;

    }



}
