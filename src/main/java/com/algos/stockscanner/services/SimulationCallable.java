package com.algos.stockscanner.services;

import com.algos.stockscanner.beans.ContextStore;
import com.algos.stockscanner.data.entity.MarketIndex;
import com.algos.stockscanner.data.service.MarketIndexService;
import com.algos.stockscanner.strategies.*;
import com.algos.stockscanner.task.AbortedByUserException;
import com.algos.stockscanner.task.TaskHandler;
import com.algos.stockscanner.task.TaskListener;
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
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
@Scope("prototype")
public class SimulationCallable implements Callable<Void> {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    LocalDate startDate;
    private int indexId;
    private float initialAmount;
    int sl;
    int tp;
    int numDays;
    private StrategyParams strategyParams;

    private MarketIndex marketIndex;

    private ConcurrentLinkedQueue<TaskListener> listeners = new ConcurrentLinkedQueue<>();
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private boolean running;
    private boolean abort;
    private Progress currentProgress;

    @Autowired
    private ContextStore contextStore;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private MarketIndexService marketIndexService;

    public SimulationCallable(LocalDate startDate, int indexId, float initialAmount, int sl, int tp, int numDays, StrategyParams strategyParams) {
        this.startDate = startDate;
        this.indexId = indexId;
        this.initialAmount=initialAmount;
        this.sl=sl;
        this.tp=tp;
        this.numDays=numDays;
        this.strategyParams=strategyParams;
    }

    private String getLogString(){
        return("date="+startDate+", index=" + marketIndex.getSymbol());
    }


    @PostConstruct
    private void init() {

        marketIndex=marketIndexService.get(indexId).get();

        log.info("Simulation task created for "+getLogString());

        // register itself to the context-level storage
        contextStore.simulationCallableMap.put("" + hashCode(), this);

        currentProgress=new Progress();

        // puts the task in 'waiting for start' status
        currentProgress.update("waiting...");

    }


    @Override
    public Void call() {

        // if already aborted before starting,
        // unregister itself from the context-level storage and return
        if(abort){
            contextStore.simulationCallableMap.remove("" + hashCode(), this);
            return null;
        }

        log.debug("Simulation task started for " + getLogString());

        running=true;

        // if is already aborted, don't perform the task
        startTime = LocalDateTime.now();

        // long task, can throw exception
        try {


            // do the stuff
            doTheStuff();

//            for(int i=0; i<10; i++){
//                checkAbort();   // throws exception if the task is aborted
//                notifyProgress(i, 9, marketIndex.getSymbol()+", A="+amplitude+", L="+lookback);
//                Thread.sleep(1000);
//            }


            endTime = LocalDateTime.now();
            String info = buildDurationInfo();
            log.info("Simulation task completed for "+getLogString());
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
                abort=true;

                // if not running yet (the task is scheduled and is in a wait state)
                // call immediately the abort procedure
                if(!running){
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


    private void checkAbort() throws Exception{
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
    private void terminateWithError(Exception e){
        endTime = LocalDateTime.now();
        if(e instanceof AbortedByUserException){
            log.info("Simulation task aborted by user for "+getLogString());
        }else{
            log.error("Simulation task error for " + getLogString(), e);
        }
        notifyError(e);
    }


    class Progress{
        private int current;
        private int tot;
        private String status;

        public void update(int current, int tot, String status){
            this.current=current;
            this.tot=tot;
            this.status=status;
        }

        public void update(String status){
            this.status=status;
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



    private void doTheStuff(){

        Strategy strategy =  context.getBean(SurferStrategy.class);



        // prepare params
        MarketIndex index = marketIndexService.get(indexId).get();
        StrategyParamsOld params = new StrategyParamsOld();
        params.setIndex(index);
        params.setStartDate(startDate);
        params.setFixedDays(generator.getFixedDays());
        LocalDate endDate = params.getStartDate().plusDays(generator.getDays() - 1);
        if (generator.getFixedDays()) {   // Fixd length
            params.setEndDate(endDate);
        } else {  // Variable length
            if (generator.getDays() > 0) {
                params.setEndDate(endDate);
            }
        }
        params.setInitialAmount(utils.toPrimitive(generator.getAmount()));
        params.setSl(utils.toPrimitive(generator.getStopLoss()));
        params.setTp(utils.toPrimitive(generator.getTakeProfit()));
        params.setAmplitude(amplitude);
        params.setSpreadPercent(utils.toPrimitive(index.getSpreadPercent()));
        params.setDaysLookback(lookback);

        // run the strategy and retrieve a Simulation
        strategy = context.getBean(SurferStrategyOld.class, params);
        simulation = strategy.execute();

        // assign the Simulation to the Generator and save
        if (simulation != null) {
            simulation.setGenerator(generator);
            simulationService.update(simulation);
        }


    }


}
