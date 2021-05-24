package com.algos.stockscanner.runner;

import com.algos.stockscanner.beans.Utils;
import com.algos.stockscanner.data.entity.Generator;
import com.algos.stockscanner.data.entity.MarketIndex;
import com.algos.stockscanner.data.service.GeneratorService;
import com.algos.stockscanner.data.service.MarketIndexService;
import com.algos.stockscanner.data.service.SimulationService;
import com.algos.stockscanner.exceptions.RunnerException;
import com.algos.stockscanner.services.SimulationCallable;
import com.algos.stockscanner.strategies.Strategy;
import com.algos.stockscanner.strategies.SurferStrategy;
import com.algos.stockscanner.views.generators.GeneratorModel;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

@Service
public class RunnerService {

    private ExecutorService executorService;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private GeneratorService generatorService;

    @Autowired
    private MarketIndexService marketIndexService;

    @Autowired
    private SimulationService simulationService;

    @Autowired
    private Utils utils;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @PostConstruct
    private void init(){
        executorService=Executors.newFixedThreadPool(4);
    }

//    public GeneratorRunner run(Generator generator, UI ui)  {
//
//        GeneratorRunner runner = context.getBean(GeneratorRunner.class, generator, ui);
//        ExecutorService executorService = Executors.newFixedThreadPool(4);
//        executorService.submit(runner);
//
//        return runner;
//
//    }


    public SimulationCallable startGenerator(GeneratorModel model) throws Exception {

        Generator generator=generatorService.get(model.getId()).get();

        preliminaryChecks(generator);

        // delete all previous simulations for this generator
        simulationService.deleteBy(generator);

        // build cartesian list of permutable properties
        List<List<Integer>> cartesianList=null;
        cartesianList=buildCartesianList(generator);

        // iterate cartesian list and spans, create and submit the SimulationCallables
        int numSpans = generator.getSpans();

        // prepare a list of strategies to execute
        List<Strategy> strategies=new ArrayList<>();
        for (List<Integer> permutation : cartesianList) {

            int indexId = permutation.get(0);
            int amplitude = permutation.get(1);
            int lookback = permutation.get(2);

            LocalDate startDate = generator.getStartDateLD();
            for (int nspan = 0; nspan < numSpans; nspan++) {
                int numDays=generator.getDays();
                startDate = startDate.plusDays(numDays*nspan);
                float amount = generator.getAmount();
                int sl = generator.getStopLoss();
//                int tp=generator.getTakeProfit();
                MarketIndex index = marketIndexService.get(indexId).get();
                Strategy strategy = context.getBean(SurferStrategy.class, index, startDate, numDays, amount, sl, amplitude, lookback);
                strategies.add(strategy);
            }
        }

        // create a SimulationCallable and assign the strategies
        SimulationCallable callable = context.getBean(SimulationCallable.class, strategies, generator.getId());
        executorService.submit(callable);

        return callable;
    }





    private  List<List<Integer>> buildCartesianList(Generator generator) throws Exception {
        List<Integer> indexIds = getIndexIdsList(generator);
        List<Integer> amplitudes = getAmplitudesList(generator);
        Collections.sort(amplitudes);
        List<Integer> lookbacks = getLookbacksList(generator);
        Collections.sort(lookbacks);
        return Lists.cartesianProduct(indexIds, amplitudes, lookbacks);
    }



    private List<Integer> getIndexIdsList(Generator generator) throws Exception {
        List<Integer> list = new ArrayList<>();
        if (utils.toPrimitive(generator.getIndexesPermutate())) {
            for (MarketIndex index : generator.getIndexes()) {
                list.add(index.getId());
            }
        } else {
            list.add(generator.getIndex().getId());
        }
        return list;
    }

    private List<Integer> getAmplitudesList(Generator generator) throws Exception {

        List<Integer> list = new ArrayList<>();

        if (generator.getAmplitudePermutate()) {
            int min = utils.toPrimitive(generator.getAmplitudeMin());
            int max = utils.toPrimitive(generator.getAmplitudeMax());
            int steps = utils.toPrimitive(generator.getAmplitudeSteps());
            list.addAll(rangeToList(min, max, steps));
        } else {
            list.add(utils.toPrimitive(generator.getAmplitude()));
        }
        return list;
    }

    private List<Integer> getLookbacksList(Generator generator) throws Exception {

        List<Integer> list = new ArrayList<>();

        if (generator.getAvgDaysPermutate()) {
            int min = utils.toPrimitive(generator.getAvgDaysMin());
            int max = utils.toPrimitive(generator.getAvgDaysMax());
            int steps = utils.toPrimitive(generator.getAvgDaysSteps());
            list.addAll(rangeToList(min, max, steps));
        } else {
            list.add(utils.toPrimitive(generator.getAvgDays()));
        }
        return list;
    }


    private List<Integer> rangeToList(int max, int min, int steps) throws Exception {
        List<Integer> list = new ArrayList<>();

        double d = (max - min) / (steps - 1);
        if (d % 1 != 0) {
            throw new Exception("Internal error, wrong number of steps!");
        }
        int step = (int) d;
        for (int i = 0; i < steps; i++) {
            Integer integer = new Integer(min + (i * step));
            list.add(integer);
        }
        return list;
    }


    private void preliminaryChecks(Generator generator) throws RunnerException {

        // build a list of market indices
        List<MarketIndex> marketIndexes = new ArrayList<>();
        if (utils.toPrimitive(generator.getIndexesPermutate())) {
            for (MarketIndex marketIndex : generator.getIndexes()) {
                marketIndexes.add(marketIndex);
            }
        } else {
            marketIndexes.add(generator.getIndex());
        }

        // check not empty
        if (marketIndexes.size() == 0) {
            throw new RunnerException("The Generator does not have Market Indexes specified");
        }

        // check that all the indexes haves data
        for (MarketIndex marketIndex : marketIndexes) {
            int count = marketIndexService.countDataPoints(marketIndex);
            if (count == 0) {
                String msg = "The index " + marketIndex.getSymbol() + " has no historic data. Download data for the index.";
                throw new RunnerException(msg);
            }
        }

        // start date
        if (generator.getStartDate() == null) {
            throw new RunnerException("Start date is not specified");
        }

        // amount
        if (utils.toPrimitive(generator.getAmount()) == 0) {
            throw new RunnerException("Initial amount is not specified");
        }

        // number of days is required
        if (utils.toPrimitive(generator.getDays()) == 0) {
            throw new RunnerException("Fixed length but no number of days specified");
        }

        // number of spans
        if (utils.toPrimitive(generator.getSpans()) == 0) {
            throw new RunnerException("Number of spans is not specified");
        }

        // amplitude
        if (utils.toPrimitive(generator.getAmplitudePermutate())) {
            int min = utils.toPrimitive(generator.getAmplitudeMin());
            int max = utils.toPrimitive(generator.getAmplitudeMax());
            int steps = utils.toPrimitive(generator.getAmplitudeSteps());

            if (min <= 0) {
                throw new RunnerException("Minimum amplitude is not specified");
            }
            if (max <= 0) {
                throw new RunnerException("Maximum amplitude is not specified");
            }
            if (steps <= 0) {
                throw new RunnerException("Amplitude step is not specified");
            }
            if (steps == 1) {
                throw new RunnerException("Amplitude steps must be > 1");
            }
            if (!verifySteps(min, max, steps)) {
                throw new RunnerException("Amplitude: # of steps doesn't fit with min-max range");
            }
        } else {
            if (utils.toPrimitive(generator.getAmplitude() == 0)) {
                throw new RunnerException("Amplitude is not specified");
            }
        }

        // lookback days
        if (utils.toPrimitive(generator.getAvgDaysPermutate())) {
            int min = utils.toPrimitive(generator.getAvgDaysMin());
            int max = utils.toPrimitive(generator.getAvgDaysMax());
            int steps = utils.toPrimitive(generator.getAvgDaysSteps());

            if (min <= 0) {
                throw new RunnerException("Minimum lookback days are not specified");
            }
            if (max <= 0) {
                throw new RunnerException("Maximum lookback days are not specified");
            }
            if (steps <= 0) {
                throw new RunnerException("Lookback days step is not specified");
            }
            if (steps == 1) {
                throw new RunnerException("Lookback steps must be > 1");
            }
            if (!verifySteps(min, max, steps)) {
                throw new RunnerException("Lookback days: # of steps doesn't fit with min-max range");
            }
        } else {
            if (utils.toPrimitive(generator.getAvgDays() == 0)) {
                throw new RunnerException("Lookback days are not specified");
            }
        }


    }

    private boolean verifySteps(int min, int max, int steps) {
        int diff = max - min;
        int rest = diff % (steps - 1);
        return rest == 0;
    }



}
