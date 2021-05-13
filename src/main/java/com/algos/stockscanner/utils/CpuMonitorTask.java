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

    // how quick to adapt to the desired rate
    private static final double ADAPTATION_SPEED =2;

    @Value("${app.cpu.limit:0.0}")
    private double appCpuLimit;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private OperatingSystemMXBean operatingSystemMXBean;
    private CpuMonitorListener listener;
    private int currentDelayIndex=0;

    public CpuMonitorTask(CpuMonitorListener listener) {
        this.listener=listener;
        operatingSystemMXBean =(OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        new Timer().schedule(this, 0, 1000);

    }

    @Override
    public void run() {

        if(appCpuLimit<=0){
            return;
        }

        double load=operatingSystemMXBean.getProcessCpuLoad();
        load = (double)Math.round(load * 10000d) / 10000d;  // round to 4 decimals


        if(load > appCpuLimit){
            currentDelayIndex++;
        }else{
            if(currentDelayIndex>0){
                currentDelayIndex--;
            }
        }

        int delayMs=(int) Math.round(currentDelayIndex * ADAPTATION_SPEED);

        log.info("cpu load: "+load+" - delay ms: "+delayMs);

        listener.delayReceived(delayMs);

    }


}
