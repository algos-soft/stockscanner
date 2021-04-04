package com.algos.stockscanner.services;

import org.springframework.stereotype.Service;

@Service
public class MarketService {


    public void download(DownloadProgressListener downloadProgressListener) {

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        downloadProgressListener.onDownloadCompleted();
    }

    public interface DownloadProgressListener{
        void onDownloadCompleted();
    };

}
