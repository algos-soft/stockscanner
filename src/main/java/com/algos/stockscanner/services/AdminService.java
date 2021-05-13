package com.algos.stockscanner.services;

import com.algos.stockscanner.beans.ContextStore;
import com.algos.stockscanner.beans.Utils;
import com.algos.stockscanner.data.service.MarketIndexService;
import com.algos.stockscanner.enums.PriceUpdateModes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
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

    @Autowired
    private MarketIndexService marketIndexService;

    private ExecutorService executorService;

    private final Logger log = LoggerFactory.getLogger(this.getClass());


    public AdminService() {
    }

    @PostConstruct
    private void init(){
        int numThreads = Runtime.getRuntime().availableProcessors() + 1;
        executorService = Executors.newFixedThreadPool(numThreads);
    }

    public UpdatePricesCallable scheduleUpdate(List<String> symbols, PriceUpdateModes mode, int maxReqPerMinute)  {
        UpdatePricesCallable callable = context.getBean(UpdatePricesCallable.class, symbols, mode, maxReqPerMinute);
        executorService.submit(callable);
        return callable;
    }


    public DownloadIndexCallable scheduleDownload(List<String> symbols, int maxReqPerMinute){
        DownloadIndexCallable callable = context.getBean(DownloadIndexCallable.class, symbols, maxReqPerMinute);
        executorService.submit(callable);
        return callable;
    }



}
