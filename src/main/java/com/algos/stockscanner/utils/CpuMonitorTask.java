package com.algos.stockscanner.utils;

import com.sun.management.OperatingSystemMXBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.util.Timer;
import java.util.TimerTask;

@Component
@Scope("prototype")
public class CpuMonitorTask extends TimerTask {

    @Value("${app.cpu.limit:0.0}")
    private double appCpuLimit;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private OperatingSystemMXBean operatingSystemMXBean;
    private static final int cpuDelayFactor=100;
    private CpuMonitorListener listener;

    public CpuMonitorTask(CpuMonitorListener listener) {
        this.listener=listener;
        operatingSystemMXBean =(OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        new Timer().schedule(this, 0, 2000);

    }

    @Override
    public void run() {
        double load=operatingSystemMXBean.getProcessCpuLoad();
        log.debug("cpu load "+(int)(load*100)+"%");

        if(load > appCpuLimit){
            double excessLoad = load-appCpuLimit;
            if(excessLoad>0){
                listener.delayReceived((int)(excessLoad*cpuDelayFactor));
            }else{
                listener.delayReceived(0);
            }

        }else{
            listener.delayReceived(0);
        }

    }


}
