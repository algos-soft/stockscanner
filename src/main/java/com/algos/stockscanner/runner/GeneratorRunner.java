package com.algos.stockscanner.runner;

import com.algos.stockscanner.beans.Utils;
import com.algos.stockscanner.data.entity.Generator;
import com.algos.stockscanner.data.entity.MarketIndex;
import com.algos.stockscanner.data.entity.Simulation;
import com.algos.stockscanner.data.service.GeneratorService;
import com.algos.stockscanner.data.service.MarketIndexService;
import com.algos.stockscanner.data.service.SimulationService;
import com.algos.stockscanner.strategies.Strategy;
import com.algos.stockscanner.strategies.StrategyParams;
import com.algos.stockscanner.strategies.SurferStrategy;
import com.algos.stockscanner.views.simulations.SimulationsView;
import com.google.common.collect.Lists;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.IronIcon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.server.Command;
import lombok.extern.slf4j.Slf4j;
import org.claspina.confirmdialog.ConfirmDialog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;


/**
 * Runs a Generator in a separate thread
 */
@Component
@Scope("prototype")
@CssImport(value = "./views/runner/generator-runner.css")
@Slf4j
public class GeneratorRunner extends VerticalLayout implements Callable<Void> {

    private Generator generator;

    private RunnerListener runnerListener;

    private Label label;

    private Button button;

    private ProgressBar progressBar;

    private final UI ui;

    private Div imgPlaceholder;

    private Icon closeIcon;

    private Exception exception;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private boolean error;  // error during execution
    private boolean abort;  // user aborted
    private boolean completed;

    private Strategy strategy;

    @Autowired
    private MarketIndexService marketIndexService;

    @Autowired
    private SimulationService simulationService;

    @Autowired
    private GeneratorService generatorService;


    @Autowired
    private Utils utils;

    @Autowired
    private ApplicationContext context;


    public GeneratorRunner(Generator generator, UI ui) {
        this.generator = generator;
        this.ui = ui;
    }

    @PostConstruct
    private void init() {

        setId("main-layout");

        Label emptyLabel = new Label(); // blank element, for alignment purpose only
        emptyLabel.getStyle().set("display", "flex");
        emptyLabel.getStyle().set("flex", "1");
        emptyLabel.getStyle().set("max-width", "1em");

        closeIcon = VaadinIcon.CLOSE.create();
        closeIcon.getStyle().set("display", "flex");
        closeIcon.getStyle().set("flex", "1");
        closeIcon.getStyle().set("max-width", "1em");
        closeIcon.getStyle().set("font-size", "0.8em");
        closeIcon.addClickListener((ComponentEventListener<ClickEvent<Icon>>) iconClickEvent -> {
            if (completed || error) {
                fireClosed();
            }
        });

        label = new Label();
        label.setId("label");
        label.getStyle().set("display", "flex");
        label.getStyle().set("flex", "1");
        label.getStyle().set("justify-content", "center");

        imgPlaceholder = new Div();
        imgPlaceholder.addClickListener((ComponentEventListener<ClickEvent<Div>>) divClickEvent -> infoClicked());
        setImage("RUN");

        progressBar = new ProgressBar();
        progressBar.setId("progress-bar");

        button = new Button("Abort");
        button.setId("button");

        button.addClickListener((ComponentEventListener<ClickEvent<Button>>) event -> {
            if (completed || error) {
                showSimulations();
            } else {
                abort = true;
                if (strategy != null) {
                    strategy.abort();
                }
                fireAborted();
            }
        });

        setProgress(0, 0, null);   // initialize the progress status

        HorizontalLayout row1 = new HorizontalLayout();
        row1.setWidth("100%");
        row1.setAlignItems(Alignment.CENTER);
        row1.add(emptyLabel, label, closeIcon);

        HorizontalLayout row2 = new HorizontalLayout();
        row2.setAlignItems(Alignment.CENTER);
        row2.add(imgPlaceholder, progressBar, button);

        add(row1, row2);

    }

    @Override
    public Void call() {

        try {

            log.info("Generator id: "+generator.getId()+" #"+generator.getNumber()+": generation started");

            startTime = LocalDateTime.now();

            // here the business logic cycle, can throw exceptions

            preliminaryChecks();

            // delete previous simulations for this generator
            simulationService.deleteBy(generator);

            // build cartesian list of permutable properties
            List<Integer> indexIds = getIndexIdsList();
            List<Integer> amplitudes = getAmplitudesList();
            Collections.sort(amplitudes);
            List<Integer> lookbacks = getLookbacksList();
            Collections.sort(lookbacks);
            List<List<Integer>> cartesianList = Lists.cartesianProduct(indexIds, amplitudes, lookbacks);

            int s = 0;
            int numPerm = cartesianList.size();
            int numSpans = generator.getSpans();

            for (List<Integer> permutation : cartesianList) {
                if (abort) {
                    break;
                }

                int indexId = permutation.get(0);
                int amplitude = permutation.get(1);
                int lookback = permutation.get(2);

                LocalDate startDate = generator.getStartDateLD();
                Simulation simulation = null;

                for (int nspan = 0; nspan < numSpans; nspan++) {
                    if (abort) {
                        break;
                    }

                    // start new span the day after the previous span ended
                    if (nspan > 0) {
                        startDate = simulation.getEndTsLDT().toLocalDate().plusDays(1);
                    }

                    // update progress
                    s++;
                    setProgress(numPerm * numSpans, s, null);

                    // prepare params
                    MarketIndex index = marketIndexService.get(indexId).get();
                    StrategyParams params = new StrategyParams();
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
                    strategy = context.getBean(SurferStrategy.class, params);
                    simulation = strategy.execute();

                    // assign the Simulation to the Generator and save
                    if (simulation != null) {
                        simulation.setGenerator(generator);
                        simulationService.update(simulation);
                    }

                }


            }

            // end of business logic cycle

            endTime = LocalDateTime.now();

            setCompleted();

            log.info("Generator id: "+generator.getId()+" #"+generator.getNumber()+": generation completed");


        } catch (RunnerException e) {

            exception = e;
            error = true;

            ui.access((Command) () -> {
                closeIcon.getStyle().set("color", "red");
                button.setEnabled(false);
                setProgress(1, 0, "Error - (!) for info");
            });
            setImage("ERR");

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private void infoClicked() {
        if (error) {
            ConfirmDialog dialog = ConfirmDialog.createError().withCaption("Error info").withMessage(exception.getMessage());
            dialog.setCloseOnOutsideClick(true);
            dialog.open();
        }
        if (completed) {
            Duration dur = Duration.between(startTime, endTime);
            long millis = dur.toMillis();

            String timeString = String.format("%02dh %02dm %02ds",
                    TimeUnit.MILLISECONDS.toHours(millis),
                    TimeUnit.MILLISECONDS.toMinutes(millis) -
                            TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                    TimeUnit.MILLISECONDS.toSeconds(millis) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));

            ConfirmDialog dialog = ConfirmDialog.createInfo().withCaption("Success").withMessage("Generation completed successfully in " + timeString);
            dialog.setCloseOnOutsideClick(true);
            dialog.open();
        }
    }

    /**
     * Update the progress bar.
     * <br>
     * If total = 0 puts the bar in indeterminate mode
     */
    private void setProgress(int total, int current, String message) {
        Command command;
        if (total > 0) {
            command = (Command) () -> {
                progressBar.setIndeterminate(false);
                progressBar.setMax(total);
                progressBar.setValue(current);
                String text = "[" + generator.getNumber() + "]";
                if (message != null) {
                    label.setText(text + " " + message);
                } else {
                    label.setText(text + " " + current + "/" + total);
                }
            };
        } else {
            command = (Command) () -> {
                progressBar.setIndeterminate(true);
                String text = "[" + generator.getNumber() + "]";
                if (message != null) {
                    label.setText(text + " " + message);
                } else {
                    label.setText(text + " running...");
                }
            };
        }

        ui.access(command);

    }

    private void setCompleted() {
        completed = true;
        ui.access((Command) () -> {
            closeIcon.getStyle().set("color", "red");
            button.setText("Show");
            progressBar.setMax(1);
            progressBar.setValue(1);
            progressBar.setIndeterminate(false);
        });
        setImage("END");
    }


    private void setImage(String type) {
        IronIcon image = null;
        String color = null;
        switch (type) {
            case "RUN":
                image = new IronIcon("vaadin", "cog");
                break;
            case "END":
                image = new IronIcon("vaadin", "check-circle");
                color = "green";
                break;
            case "ERR":
                image = new IronIcon("vaadin", "exclamation-circle");
                color = "red";
                break;
        }

        assert image != null;
        image.setId("image");
        if (color != null) {
            image.getStyle().set("color", color);
        } else {
            image.getStyle().remove("color");
        }

        IronIcon finalImage = image;
        ui.access((Command) () -> {
            imgPlaceholder.removeAll();
            imgPlaceholder.add(finalImage);
        });

    }

    public interface RunnerListener {
        void onAborted();

        void onClosed();
    }

    public void setRunnerListener(RunnerListener runnerListener) {
        this.runnerListener = runnerListener;
    }


    private void fireAborted() {
        if (runnerListener != null) {
            runnerListener.onAborted();
        }
    }

    private void fireClosed() {
        if (runnerListener != null) {
            runnerListener.onClosed();
        }
    }


    void preliminaryChecks() throws RunnerException {

        // build a list of market indices
        List<MarketIndex> marketIndexes=new ArrayList<>();
        if(generator.getIndexesPermutate()){
            for(MarketIndex marketIndex : generator.getIndexes()){
                marketIndexes.add(marketIndex);
            }
        }else{
            marketIndexes.add(generator.getIndex());
        }

        // check not empty
        if(marketIndexes.size()==0){
            throw new RunnerException("The Generator does not have Market Indexes specified");
        }

        // check that all the indexes haves data
        for(MarketIndex marketIndex : marketIndexes){
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

        // if fixed length, number of days is required
        if (generator.getFixedDays()) {
            if (utils.toPrimitive(generator.getDays()) == 0) {
                throw new RunnerException("Fixed length but no number of days specified");
            }
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


    private void showSimulations() {
        UI.getCurrent().navigate(SimulationsView.class, "" + generator.getNumber());
    }

    private List<Integer> getIndexIdsList() throws Exception {
        List<Integer> list = new ArrayList<>();
        if (generator.getIndexesPermutate()) {
            for (MarketIndex index : generator.getIndexes()) {
                list.add(index.getId());
            }
        } else {
            list.add(generator.getIndex().getId());
        }
        return list;
    }

    private List<Integer> getAmplitudesList() throws Exception {

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

    private List<Integer> getLookbacksList() throws Exception {

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


}
