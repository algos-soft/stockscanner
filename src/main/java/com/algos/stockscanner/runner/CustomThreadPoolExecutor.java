package com.algos.stockscanner.runner;

import java.util.concurrent.*;

public class CustomThreadPoolExecutor extends ThreadPoolExecutor {

    public CustomThreadPoolExecutor() {
        super(  1, // core threads
                1, // max threads
                1, // timeout
                TimeUnit.MINUTES, // timeout units
                new LinkedBlockingQueue<Runnable>() // work queue
        );
    }

    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        if (t == null && r instanceof Future<?>) {
            try {
                Future<?> future = (Future<?>) r;
                if (future.isDone()) {
                    future.get();
                }
            } catch (CancellationException ce) {
                t = ce;
            } catch (ExecutionException ee) {
                t = ee.getCause();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
        if (t != null) {
            //throw t;
            System.out.println(t);
        }
    }



}
