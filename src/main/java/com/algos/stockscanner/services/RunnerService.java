package com.algos.stockscanner.services;

import com.algos.stockscanner.data.entity.Generator;

import com.algos.stockscanner.runner.GeneratorMonitor;
import com.algos.stockscanner.runner.GeneratorRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;

@Service
public class RunnerService {

    @Autowired
    private ApplicationContext context;

    public GeneratorMonitor run(Generator generator) throws Exception {
        int a = 87;
        int b= a;


        GeneratorRunner runner = context.getBean(GeneratorRunner.class);
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        Future<String> future = executorService.submit(runner);
// some operations
        GeneratorMonitor monitor = context.getBean(GeneratorMonitor.class);

        String result = future.get();



//        Executor executor = Executors.newSingleThreadExecutor();

//        ThreadPoolExecutor executor = (ThreadPoolExecutor)Executors.newFixedThreadPool(2);
//        GeneratorRunner runner = new GeneratorRunner();
//        executor.submit(runner);

        //throw new Exception("Not implemented.");

        return monitor;
    }




}
