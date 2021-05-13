package com.algos.stockscanner.services;

import com.algos.stockscanner.beans.ContextStore;
import com.algos.stockscanner.data.entity.Generator;
import com.algos.stockscanner.data.entity.MarketIndex;
import com.algos.stockscanner.data.entity.Simulation;
import com.algos.stockscanner.data.service.GeneratorService;
import com.algos.stockscanner.data.service.MarketIndexService;
import com.algos.stockscanner.data.service.SimulationService;
import com.algos.stockscanner.strategies.*;
import com.algos.stockscanner.task.AbortedByUserException;
import com.algos.stockscanner.task.TaskHandler;
import com.algos.stockscanner.task.TaskListener;
import com.algos.stockscanner.utils.CpuMonitorListener;
import com.algos.stockscanner.utils.CpuMonitorTask;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
@Scope("prototype")
public class SimulationCallable implements Callable<Void> {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

//    LocalDate startDate;
//    private int indexId;
//    private float initialAmount;
//    int sl;
//    int tp;
//    int numDays;
//    private StrategyParams strategyParams;

//    private MarketIndex marketIndex;

    private List<Strategy> strategies;
    private int generatorId;

    private ConcurrentLinkedQueue<TaskListener> listeners = new ConcurrentLinkedQueue<>();
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private boolean running;
    private boolean abort;
    private Progress currentProgress;

    private TimerTask cpuTimer;
    private int cpuPauseMs;

    @Autowired
    private ContextStore contextStore;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private MarketIndexService marketIndexService;

    @Autowired
    private SimulationService simulationService;

    @Autowired
    private GeneratorService generatorService;

    public SimulationCallable(List<Strategy> strategies, int generatorId) {
        this.strategies=strategies;
        this.generatorId=generatorId;
    }


    @PostConstruct
    private void init() {

        // register itself to the context-level storage
        contextStore.simulationCallableMap.put("" + hashCode(), this);

        currentProgress = new Progress();

        // puts the task in 'waiting for start' status
        currentProgress.update("waiting...");

        // start a thread to monitor the CPU load
        cpuTimer = context.getBean(CpuMonitorTask.class, (CpuMonitorListener) delayMs -> {
            cpuPauseMs =delayMs;
            // invoke the current StrategyHandler
        });

    }


    @Override
    public Void call() {


        // if already aborted before starting,
        // unregister itself from the context-level storage and return
        if (abort) {
            contextStore.simulationCallableMap.remove("" + hashCode(), this);
            return null;
        }

        log.info("Task started for generator id " + generatorId);

        running = true;

        // if is already aborted, don't perform the task
        startTime = LocalDateTime.now();

        // long task, can throw exception
        try {

            Simulation simulation;
            Generator generator = generatorService.get(generatorId).get();
            int i=0;
            for(Strategy strategy : strategies){

                // obtain a strategy handler from the Strategy,
                // maintain it in this object as the current strategy handler,
                // and invoke the appropriate method anytime the cpuTimer receives a retroaction value.
                //StrategyHandler handler = strategy.getStrategyHandler();

                checkAbort();
                i++;
                log.info("Starting strategy: " + strategy);
                simulation = strategy.execute();
                simulation.setGenerator(generator);
                simulationService.update(simulation);
                log.info("Strategy completed: " + strategy);
                notifyProgress(i, strategies.size(), ""+strategy);
            }

//            strategy.addProgressListener(new StrategyProgressListener() {
//                @Override
//                public void notifyProgress(int current, int total, String status) {
//                    SimulationCallable.this.notifyProgress(current, total, status);
//                }
//            });


            endTime = LocalDateTime.now();
            String info = buildDurationInfo();
            log.info("Task completed for generator id " + generatorId);
            notifyCompleted(info);

        } catch (Exception e) {

            terminateWithError(e);

        } finally {

            // unregister itself from the context-level storage
            contextStore.simulationCallableMap.remove("" + hashCode(), this);

        }

        return null;

    }


    private String buildDurationInfo() {
        Duration duration = Duration.between(startTime, endTime);
        String sDuration = DurationFormatUtils.formatDuration(duration.toMillis(), "H:mm:ss", true);
        DateTimeFormatter format = DateTimeFormatter.ofPattern("HH:mm:ss");
        return "start: " + startTime.format(format) + ", end: " + endTime.format(format) + ", elapsed: " + sDuration;
    }


    /**
     * @param listener listener to inform with progress events
     */
    public void addListener(TaskListener listener) {
        this.listeners.add(listener);

        // update the listener as soon as it attaches
        listener.onProgress(currentProgress.getCurrent(), currentProgress.getTot(), currentProgress.getStatus());

    }


    /**
     * @return provide a handler to interrupt/manage the execution
     */
    public TaskHandler obtainHandler() {
        TaskHandler handler = new TaskHandler() {
            @Override
            public void abort() {

                // turn on the abort flag
                abort = true;

                // if not running yet (the task is scheduled and is in a wait state)
                // call immediately the abort procedure
                if (!running) {
                    try {
                        checkAbort();
                    } catch (Exception e) {
                        terminateWithError(e);
                    }
                }
            }
        };
        return handler;
    }


    private void notifyProgress(int current, int tot, String info) {

        currentProgress.update(current, tot, info);

        for (TaskListener listener : listeners) {
            listener.onProgress(current, tot, info);
        }
    }

    private void notifyError(Exception e) {
        for (TaskListener listener : listeners) {
            listener.onError(e);
        }
    }

    private void notifyCompleted(String info) {
        for (TaskListener listener : listeners) {
            listener.onCompleted(info);
        }
    }


    private void checkAbort() throws Exception {
        if (abort) {
            currentProgress.update("Aborted");
            notifyProgress(currentProgress.getCurrent(), currentProgress.getTot(), currentProgress.getStatus());
            throw new AbortedByUserException();
        }
    }


    /**
     * Sets the end timestamp, logs the error and
     * notifies the listeners
     */
    private void terminateWithError(Exception e) {
        endTime = LocalDateTime.now();
        if (e instanceof AbortedByUserException) {
            log.info("Task aborted by user for generator " + generatorId);
        } else {
            log.error("Task error for generator " + generatorId, e);
        }
        notifyError(e);
    }


    class Progress {
        private int current;
        private int tot;
        private String status;

        public void update(int current, int tot, String status) {
            this.current = current;
            this.tot = tot;
            this.status = status;
        }

        public void update(String status) {
            this.status = status;
        }

        public int getCurrent() {
            return current;
        }

        public int getTot() {
            return tot;
        }

        public String getStatus() {
            return status;
        }
    }



}
