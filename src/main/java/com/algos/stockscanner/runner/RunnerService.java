package com.algos.stockscanner.runner;

import com.algos.stockscanner.data.entity.Generator;
import com.vaadin.flow.component.UI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
public class RunnerService {

    @Autowired
    private ApplicationContext context;

    public GeneratorRunner run(Generator generator, UI ui)  {
        int a = 87;
        int b= a;





        GeneratorRunner runner = context.getBean(GeneratorRunner.class, generator, ui);
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        executorService.submit(runner);



// some operations
//        GeneratorMonitor monitor = context.getBean(GeneratorMonitor.class);


        //String result = future.get(); // questo blocca



//        Executor executor = Executors.newSingleThreadExecutor();

//        ThreadPoolExecutor executor = (ThreadPoolExecutor)Executors.newFixedThreadPool(2);
//        GeneratorRunner runner = new GeneratorRunner();
//        executor.submit(runner);

        //throw new Exception("Not implemented.");

        return runner;
    }




}
