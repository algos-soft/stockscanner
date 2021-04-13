package com.algos.stockscanner.runner;

import com.algos.stockscanner.data.entity.Generator;
import com.algos.stockscanner.data.entity.MarketIndex;
import com.algos.stockscanner.data.service.MarketIndexService;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.IronIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.server.Command;
import org.claspina.confirmdialog.ConfirmDialog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Runs a Generator in a separate thread
 */
@Component
@Scope("prototype")
@CssImport(value="./views/runner/generator-runner.css")
public class GeneratorRunner extends VerticalLayout implements Callable<Void> {

    private final Generator generator;

    private RunnerListener RunnerListener;

    private Label label;

    private Button button;

    private ProgressBar progressBar;

    private final UI ui;

    private Div imgPlaceholder;

    private Exception exception;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private boolean error;  // error during execution
    private boolean abort;  // user aborted
    private boolean completed;

    @Autowired
    private MarketIndexService marketIndexService;

    public GeneratorRunner(Generator generator, UI ui) {
        this.generator=generator;
        this.ui=ui;
    }

    @PostConstruct
    private void init(){

        setId("main-layout");

        label = new Label();

        label.setId("label");

        imgPlaceholder = new Div();
        imgPlaceholder.addClickListener((ComponentEventListener<ClickEvent<Div>>) divClickEvent -> infoClicked());
        setImage("RUN");

        progressBar = new ProgressBar();
        progressBar.setId("progress-bar");

        button = new Button("Abort");
        button.setId("button");

        button.addClickListener((ComponentEventListener<ClickEvent<Button>>) event -> {
            if(completed || error){
                fireClosed();
            }else{
                abort=true;
                fireAborted();
            }
        });

        setProgress(0,0, null);   // initialize the progress status

        HorizontalLayout row2 = new HorizontalLayout();
        row2.add(imgPlaceholder, progressBar, button);

        add(label, row2);

    }

    @Override
    public Void call() {

        try {

            startTime = LocalDateTime.now();

            // here the business logic cycle, can throw exceptions

            // preliminary checks
            preliminaryChecks();


            int cycles=10;
            for(int i=0; i<cycles; i++){

                if(abort){
                    break;
                }

                setProgress(cycles,i+1, null);
                Thread.sleep(1000);

//                if(i==2){
//                    //throw new Exception("Eccezione");
//                }

            }
            // end of business logic cycle

            endTime=LocalDateTime.now();

            setCompleted();

        }catch (Exception e){

            exception=e;
            error=true;

            ui.access((Command) () -> {
                button.setText("Close");
                setProgress(1, 0, "Error");
            });
            setImage("ERR");

        }

        return null;
    }

    private void infoClicked(){
        if(error){
            ConfirmDialog dialog = ConfirmDialog.createError().withCaption("Error info").withMessage(exception.getMessage());
            dialog.setCloseOnOutsideClick(true);
            dialog.open();
        }
        if(completed){
            Duration dur = Duration.between(startTime, endTime);
            long millis = dur.toMillis();

            String timeString=String.format("%02dh %02dm %02ds",
                    TimeUnit.MILLISECONDS.toHours(millis),
                    TimeUnit.MILLISECONDS.toMinutes(millis) -
                            TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                    TimeUnit.MILLISECONDS.toSeconds(millis) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));

            ConfirmDialog dialog = ConfirmDialog.createInfo().withCaption("Success").withMessage("Generation completed successfully in "+timeString);
            dialog.setCloseOnOutsideClick(true);
            dialog.open();
        }
    }

    /**
     * Update the progress bar.
     * <br>
     * If total = 0 puts the bar in indeterminate mode
     */
    private void setProgress(int total, int current, String message){
        Command command;
        if(total>0){
            command = (Command) () -> {
                progressBar.setIndeterminate(false);
                progressBar.setMax(total);
                progressBar.setValue(current);
                String text="["+generator.getNumber()+"]";
                if(message!=null){
                    label.setText(text+" "+message);
                }else{
                    label.setText(text+" "+current+"/"+total);
                }
            };
        }else{
            command = (Command) () -> {
                progressBar.setIndeterminate(true);
                String text="["+generator.getNumber()+"]";
                if(message!=null){
                    label.setText(text+" "+message);
                }else{
                    label.setText(text+" running...");
                }
            };
        }

        ui.access(command);

    }

    private void setCompleted(){
        completed=true;
        ui.access((Command) () -> {
            button.setText("Close");
            progressBar.setMax(1);
            progressBar.setValue(1);
            progressBar.setIndeterminate(false);
        });
        setImage("END");
    }


    private void setImage(String type){
        IronIcon image=null;
        String color=null;
        switch (type){
            case "RUN":
                image = new IronIcon("vaadin", "cog");
                break;
            case "END":
                image = new IronIcon("vaadin", "check-circle");
                color="green";
                break;
            case "ERR":
                image = new IronIcon("vaadin", "exclamation-circle");
                color="red";
                break;
        }

        assert image != null;
        image.setId("image");
        if(color!=null){
            image.getStyle().set("color",color);
        }else{
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
        this.RunnerListener = runnerListener;
    }


    private void fireAborted(){
        if(RunnerListener !=null){
            RunnerListener.onAborted();
        }
    }

    private void fireClosed(){
        if(RunnerListener !=null){
            RunnerListener.onClosed();
        }
    }





    void preliminaryChecks() throws Exception{

        // check that a index is specified
        MarketIndex marketIndex = generator.getIndex();
        if(marketIndex==null){
            throw new Exception("The Generator does not have a Market Index specified");
        }

        // check that index has data
        MarketIndex probeIndex = new MarketIndex();
        marketIndex.setSymbol(marketIndex.getSymbol());
        int count = marketIndexService.countDataPoints(marketIndex);
        if(count==0){
            String msg = "The index "+marketIndex.getSymbol()+" has no historic data. Load data in the index first.";
            throw new Exception(msg);
        }
    }



}
