package com.algos.stockscanner.runner;

import com.algos.stockscanner.beans.Utils;
import com.algos.stockscanner.data.entity.Generator;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.IronIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.server.Command;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

/**
 * Runs a Generator in a separate thread
 */
@Component
@Scope("prototype")
@CssImport(value="./views/runner/generator-runner.css")
public class GeneratorRunner extends VerticalLayout implements Callable {

    private Generator generator;

    private RunnerListener RunnerListener;

    private Label label;

    private Button button;

    private ProgressBar progressBar;

    private UI ui;

    private Div imgPlaceholder;


    private boolean error;  // error during execution
    private boolean abort;  // user aborted
    private boolean completed;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private Utils utils;

    public GeneratorRunner(Generator generator, UI ui) {
        this.generator=generator;
        this.ui=ui;
    }

    @PostConstruct
    private void init(){

        setId("main-layout");

        label = new Label("Testo di prova");
        label.setId("label");

        imgPlaceholder = new Div();
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
                AbortedInfo info = new AbortedInfo();
                fireAborted(info);
            }
        });

        HorizontalLayout row2 = new HorizontalLayout();
        row2.add(imgPlaceholder, progressBar, button);

        add(label, row2);

    }

    @Override
    public Object call() throws Exception {

        try {

            for(int i=0; i<4; i++){
                if(abort){
                    break;
                }

                setProgress(4,i+1);
//                ProgressInfo info = new ProgressInfo();
//                info.total=4;
//                info.current=i;
//                fireProgress(info);
                Thread.sleep(1000);

                if(i==2){
                    throw new Exception("Eccezione");
                }

            }


            setCompleted();


        }catch (Exception e){

            error=true;

            ui.access((Command) () -> {
                button.setText("Close");
            });
            setImage("ERR");

//            ErrorInfo errorInfo=new ErrorInfo();
//            errorInfo.exception=e;
//            fireError(errorInfo);

        }

        return null;
    }


    private void setProgress(int total, int current){

        ui.access((Command) () -> {
            progressBar.setMax(total);
            progressBar.setValue(current);
            label.setText(current+"/"+total);
        });
    }

    private void setCompleted(){
        completed=true;
        ui.access((Command) () -> {
            button.setText("Close");
        });
        setImage("END");

//        CompletedInfo info = new CompletedInfo();
//        fireCompleted(info);

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
        void onProgress(ProgressInfo info);
        void onCompleted(CompletedInfo info);
        void onError(ErrorInfo info);
        void onAborted(AbortedInfo info);
        void onClosed();
    }

    public void setRunnerListener(RunnerListener runnerListener) {
        this.RunnerListener = runnerListener;
    }

    private void fireProgress(ProgressInfo info){
        if(RunnerListener !=null){
            RunnerListener.onProgress(info);
        }
    }

    private void fireCompleted(CompletedInfo info){
        if(RunnerListener !=null){
            RunnerListener.onCompleted(info);
        }
    }

    private void fireError(ErrorInfo info){
        if(RunnerListener !=null){
            RunnerListener.onError(info);
        }
    }

    private void fireAborted(AbortedInfo info){
        if(RunnerListener !=null){
            RunnerListener.onAborted(info);
        }
    }

    private void fireClosed(){
        if(RunnerListener !=null){
            RunnerListener.onClosed();
        }
    }


    public class ProgressInfo{
        int total;
        int current;
    }

    public class AbortedInfo{

    }

    public class ErrorInfo{
        Exception exception;
    }

    public class CompletedInfo{

    }


}
