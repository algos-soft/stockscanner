package com.algos.stockscanner.runner;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;

/**
 * Runs a Generator in a separate thread
 */
@Component
@Scope("prototype")
public class GeneratorRunner implements Callable {
    @Override
    public Object call() throws Exception {
        return null;
    }
}
