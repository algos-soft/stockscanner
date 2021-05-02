package com.algos.stockscanner.services;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;

@Component
@Scope("prototype")
public class DownloadIndexDataCallable implements Callable<DownloadIndexDataStatus> {
    @Override
    public DownloadIndexDataStatus call() throws Exception {

        Thread.sleep(3000);

        return new DownloadIndexDataStatus();

    }
}
