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

        GeneratorRunner runner = context.getBean(GeneratorRunner.class, generator, ui);
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        executorService.submit(runner);

        return runner;

    }




}
