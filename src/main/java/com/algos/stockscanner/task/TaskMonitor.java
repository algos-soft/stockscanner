package com.algos.stockscanner.task;

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
import org.claspina.confirmdialog.ConfirmDialog;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Component to monitor the status of a task
 */
@Component
@Scope("prototype")
@CssImport(value = "./views/task/task-monitor.css")
public class TaskMonitor extends VerticalLayout  {

    private Label label;

    private Button button;

    private ProgressBar progressBar;

    private Div imgPlaceholder;

    private Icon closeIcon;

    private MonitorListener monitorListener;

    private boolean error;  // error during execution
    private boolean abort;  // user aborted
    private boolean completed;

    private final UI ui;

//    private TaskListener taskListener;
//
//    private TaskHandler taskHandler;

    public TaskMonitor(UI ui, MonitorListener monitorListener) {
        this.ui = ui;
        this.monitorListener = monitorListener;
//        this.taskHandler = taskHandler;
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
//                showSimulations();
            } else {
                abort = true;
//                if (strategy != null) {
//                    strategy.abort();
//                }
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


    public void onProgress(int current, int total, Object info) {
        setProgress(current, total, info.toString());
    }

    public void onCompleted(boolean aborted) {
        setCompleted();
    }

    public void onError(Exception e) {
        setCompleted();
    }


    private void fireClosed() {
        if (monitorListener != null) {
            monitorListener.onClosed();
        }
    }

    private void fireAborted() {
        if (monitorListener != null) {
            monitorListener.onAborted();
        }
    }

    private void infoClicked() {
        if (error) {
//            ConfirmDialog dialog = ConfirmDialog.createError().withCaption("Error info").withMessage(exception.getMessage());
//            dialog.setCloseOnOutsideClick(true);
//            dialog.open();
        }
        if (completed) {
//            Duration dur = Duration.between(startTime, endTime);
//            long millis = dur.toMillis();
//
//            String timeString = String.format("%02dh %02dm %02ds",
//                    TimeUnit.MILLISECONDS.toHours(millis),
//                    TimeUnit.MILLISECONDS.toMinutes(millis) -
//                            TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
//                    TimeUnit.MILLISECONDS.toSeconds(millis) -
//                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
//
//            ConfirmDialog dialog = ConfirmDialog.createInfo().withCaption("Success").withMessage("Generation completed successfully in " + timeString);
//            dialog.setCloseOnOutsideClick(true);
//            dialog.open();
        }
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


    /**
     * Update the progress bar.
     * <br>
     * If total = 0 puts the bar in indeterminate mode
     */
    private void setProgress(int current, int total, String message) {
        Command command;
        if (total > 0) {
            command = (Command) () -> {
                progressBar.setIndeterminate(false);
                progressBar.setMax(total);
                progressBar.setValue(current);
                String text = "[" + message + "]";
                if (message != null) {
                    label.setText(text + " " + message);
                } else {
                    label.setText(text + " " + current + "/" + total);
                }
            };
        } else {
            command = (Command) () -> {
                progressBar.setIndeterminate(true);
                String text = "[" + message + "]";
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



    public interface MonitorListener {
        void onAborted();
        void onClosed();
    }




}
