package com.algos.stockscanner.services;

import com.algos.stockscanner.data.entity.Generator;

import org.springframework.stereotype.Service;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

@Service
public class RunnerService {

    public void run(Generator generator) throws Exception {
        int a = 87;
        int b= a;

//        ThreadPoolExecutor executor = (ThreadPoolExecutor)Executors.newFixedThreadPool(2);
//        GeneratorRunner runner = new GeneratorRunner();
//        executor.submit(runner);

        throw new Exception("Not implemented.");
    }




}
