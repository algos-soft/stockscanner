package com.algos.stockscanner.runner;

import com.algos.stockscanner.data.entity.Generator;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.server.Command;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
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


    private boolean abort;
    private boolean completed;

    public GeneratorRunner(Generator generator, UI ui) {
        this.generator=generator;
        this.ui=ui;
    }

    @PostConstruct
    private void init(){

        setId("main-layout");

        label = new Label("Testo di prova");
        label.setId("label");

        progressBar = new ProgressBar();
        progressBar.setId("progress-bar");

        button = new Button("Abort");
        button.setId("button");

        button.addClickListener((ComponentEventListener<ClickEvent<Button>>) event -> {
            if(completed){
                fireClosed();
            }else{
                abort=true;
                AbortedInfo info = new AbortedInfo();
                fireAborted(info);
            }
        });

        HorizontalLayout row2 = new HorizontalLayout();
        row2.add(progressBar, button);

        add(label, row2);

    }

    @Override
    public Object call() throws Exception {

        try {

            for(int i=0; i<4; i++){
                if(abort){
                    break;
                }

                setProgress(4,i);
//                ProgressInfo info = new ProgressInfo();
//                info.total=4;
//                info.current=i;
//                fireProgress(info);
                Thread.sleep(1000);
            }

            if(true){
                //throw new Exception("Eccezione");
            }

            setCompleted();


        }catch (Exception e){
            ErrorInfo errorInfo=new ErrorInfo();
            errorInfo.exception=e;
            fireError(errorInfo);
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

//        CompletedInfo info = new CompletedInfo();
//        fireCompleted(info);

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
