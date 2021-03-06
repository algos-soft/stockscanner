package com.algos.stockscanner.task;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.IronIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.server.Command;
import org.apache.commons.lang3.StringUtils;
import org.claspina.confirmdialog.ButtonOption;
import org.claspina.confirmdialog.ConfirmDialog;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Component to monitor the status of a task
 */
@Component
@Scope("prototype")
@CssImport(value = "./views/task/task-monitor.css")
public class TaskMonitor extends VerticalLayout  {

    private Label label;

    private ProgressBar progressBar;

    private HorizontalLayout imgPlaceholder;

    private IronIcon closeIcon;

    private MonitorListener monitorListener;

    // if execution is completed (successfully or not)
    private boolean executionCompleted;

    // the exception if execution was not successful
    private Exception exception;

    private Object completionInfo;

    private UI ui;

    private boolean autoClose;  // close automatically when completed successfully

    @PostConstruct
    private void init() {

        ui=UI.getCurrent();

        setId("taskmonitor-main-layout");

        closeIcon = new IronIcon("vaadin", "close");
        closeIcon.setId("taskmonitor-close-icon");
        closeIcon.addClickListener(new ComponentEventListener<ClickEvent<IronIcon>>() {
            @Override
            public void onComponentEvent(ClickEvent<IronIcon> ironIconClickEvent) {
                if (executionCompleted) {
                    fireClosed();
                }else{

                    Button bConfirm = new Button();
                    ConfirmDialog dialog = ConfirmDialog.create().withMessage("Really abort execution?")
                            .withButton(new Button(), ButtonOption.caption("Cancel"), ButtonOption.closeOnClick(true))
                            .withButton(bConfirm, ButtonOption.caption("Abort execution"), ButtonOption.focus(), ButtonOption.closeOnClick(true));

                    bConfirm.addClickListener((ComponentEventListener<ClickEvent<Button>>) event1 -> {
                        fireAborted();
                    });
                    dialog.open();

                }
            }
        });


        label = new Label();
        label.setId("taskmonitor-label");

        imgPlaceholder = new HorizontalLayout();
        imgPlaceholder.addClickListener((ComponentEventListener<ClickEvent<HorizontalLayout>>) horizontalLayoutClickEvent -> infoClicked());
        setImage("RUN");

        progressBar = new ProgressBar();
        progressBar.setId("taskmonitor-progress-bar");
        setProgress(0, 0, null);   // initialize the progress status

        HorizontalLayout row1 = new HorizontalLayout();
        row1.setSpacing(false);
        row1.setId("taskmonitor-upper-row");
        row1.setAlignItems(Alignment.CENTER);
        row1.add(imgPlaceholder, label, closeIcon);

        add(row1, progressBar);

    }


    public void onProgress(int current, int total, Object progressInfo) {
        String sInfo="";
        if(progressInfo!=null){
            sInfo=progressInfo.toString();
        }
        setProgress(current, total, sInfo);
    }

    public void onCompleted(Object completionInfo) {
        setCompleted(completionInfo);
        if(autoClose){
            fireClosed();
        }
    }

    public void onError(Exception e) {
        exception=e;
        setAborted();
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
        if(executionCompleted){
            if (exception==null) {  // completed successfully
                String sInfo="";
                if(completionInfo!=null){
                    sInfo=completionInfo.toString();
                }
                ConfirmDialog dialog = ConfirmDialog.createInfo().withCaption("Completed").withMessage(sInfo);
                dialog.setCloseOnOutsideClick(true);
                dialog.open();
            }else{                  // aborted by user or with error
                ConfirmDialog dialog = ConfirmDialog.createError().withCaption("Error").withMessage(exception.getMessage());
                dialog.setCloseOnOutsideClick(true);
                dialog.open();
            }
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
        image.setId("taskmonitor-image");
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

        StringBuilder builder=new StringBuilder();
        if(!StringUtils.isEmpty(message)){
            builder.append(message);
        }

        if(current>0){
            if(builder.length()>0){
                builder.append(" ");
            }
            builder.append("[" + current + "/" + total+"]");
        }
        final String finalmsg = builder.toString();


        if (total > 0) {
            command = (Command) () -> {
                progressBar.setIndeterminate(false);
                progressBar.setMax(total);
                progressBar.setValue(current);
                label.setText(finalmsg);
            };
        } else {
            command = (Command) () -> {
                progressBar.setIndeterminate(true);
                label.setText(finalmsg);
            };
        }

        ui.access(command);

    }


    private void setCompleted(Object completionInfo) {
        executionCompleted = true;
        this.completionInfo=completionInfo;
        ui.access((Command) () -> {
            progressBar.setMax(1);
            progressBar.setValue(1);
            progressBar.setIndeterminate(false);
        });
        setImage("END");
    }

    private void setAborted() {
        executionCompleted = true;
        ui.access((Command) () -> {
            label.setText("Error - click (!) for details");
            progressBar.setIndeterminate(false);
        });
        setImage("ERR");
    }


    public interface MonitorListener {
        void onAborted();
        void onClosed();
    }


    public void setMonitorListener(MonitorListener monitorListener) {
        this.monitorListener = monitorListener;
    }

    public boolean isAutoClose() {
        return autoClose;
    }

    public void setAutoClose(boolean autoClose) {
        this.autoClose = autoClose;
    }
}
