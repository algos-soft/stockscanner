package com.algos.stockscanner.views.admin;

import com.algos.stockscanner.beans.ContextStore;
import com.algos.stockscanner.beans.Utils;
import com.algos.stockscanner.data.entity.MarketIndex;
import com.algos.stockscanner.enums.IndexDownloadModes;
import com.algos.stockscanner.data.service.MarketIndexService;
import com.algos.stockscanner.services.*;
import com.algos.stockscanner.task.TaskHandler;
import com.algos.stockscanner.task.TaskListener;
import com.algos.stockscanner.task.TaskMonitor;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.RadioGroupVariant;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.server.Command;
import org.claspina.confirmdialog.ButtonOption;
import org.claspina.confirmdialog.ConfirmDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Component
@Scope(value = SCOPE_PROTOTYPE)
@CssImport("./views/admin/admin-view.css")
public class MarketIndexesPage  extends VerticalLayout {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ApplicationContext context;

    @Autowired
    private MarketService marketService;

    @Autowired
    private MarketIndexService marketIndexService;

    @Autowired
    private AdminService adminService;

    private @Autowired
    Utils utils;

    @Autowired
    private ContextStore contextStore;

    private HorizontalLayout statusLayout;

    private RadioButtonGroup<IndexDownloadModes> optionsGroup;

    private IntegerField limitField;

    @PostConstruct
    private void init(){

        statusLayout = new HorizontalLayout();
        statusLayout.setSpacing(false);
        statusLayout.setPadding(false);
        statusLayout.addClassName("admin-view-statuslayout");

        optionsGroup = new RadioButtonGroup<>();
        optionsGroup.setLabel("Download mode");
        optionsGroup.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        optionsGroup.setItems(IndexDownloadModes.values());
        optionsGroup.setValue(IndexDownloadModes.NEW);


        Button bDownloadIndexes = new Button("Start download");
        bDownloadIndexes.setId("adminview-bdownloadindexes");
        bDownloadIndexes.addClickListener(new ComponentEventListener<ClickEvent<Button>>() {
            @Override
            public void onComponentEvent(ClickEvent<Button> buttonClickEvent) {

                try {
                    log.info("Download Indexes action requested");
                    List<String> symbols=getSimbolsToDownload();
                    IndexDownloadModes mode = optionsGroup.getValue();
                    int intervalSec = 60/limitField.getValue();
                    List<DownloadIndexCallable> callables = adminService.scheduleDownload(mode, symbols, intervalSec);
                    for(DownloadIndexCallable callable : callables){
                        attachMonitorToTask(callable);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });


        // request limit field
        limitField=new IntegerField("Max req per minute");
        limitField.setId("adminview-reqlimitfield");
        limitField.setValue(5);

        HorizontalLayout row1 = new HorizontalLayout();
        row1.setAlignItems(Alignment.BASELINE);
        row1.add(limitField, bDownloadIndexes);

        VerticalLayout content = new VerticalLayout();
        content.setHeight("100%");
        content.add(optionsGroup, row1);

        setHeight("100%");
        add(content, statusLayout);

        // retrieve the running tasks from the context, create Task Monitors and put them in the UI
        Collection<DownloadIndexCallable> callables = contextStore.downloadIndexCallableMap.values();
        for(DownloadIndexCallable callable : callables){
            attachMonitorToTask(callable);
        }

    }



    /**
     * Build the list of symbols to download based on the currently selected mode
     */
    private List<String> getSimbolsToDownload(){
        ArrayList<String> symbols=new ArrayList<>();

        List<IndexEntry> allSymbols = marketIndexService.loadAllAvailableSymbols();
        List<String> existingSymbols=new ArrayList<>();
        List<MarketIndex> entities = marketIndexService.findAll();
        for(MarketIndex index : entities){
            existingSymbols.add(index.getSymbol());
        }
        switch (optionsGroup.getValue()){
            case NEW: // new only
                for(IndexEntry entry : allSymbols){
                    String symbol=entry.getSymbol();
                    if(!existingSymbols.contains(symbol)){
                        symbols.add(entry.getSymbol());
                    }
                }
                break;
            case UPDATE: // update existing
                break;
            case NEW_AND_UPDATE:// new and update
                break;
        }

        return symbols;
    }


    /**
     * Attach a TaskMonitor to a task and add it to the status panel
     */
    private void attachMonitorToTask(DownloadIndexCallable callable){

        UI ui = UI.getCurrent();

        // obtain a handle to interrupt/manage the task
        TaskHandler handler=callable.obtainHandler();

        // GUI component handling events coming from the monitor
        TaskMonitor taskMonitor = context.getBean(TaskMonitor.class);
        TaskMonitor.MonitorListener listener = new TaskMonitor.MonitorListener() {

            // warning, the listener is called on another thread

            @Override
            public void onAborted() {
                handler.abort();
            }

            @Override
            public void onClosed() {
                ui.access((Command) () -> statusLayout.remove(taskMonitor));
            }
        };
        taskMonitor.setMonitorListener(listener);
        //taskMonitor.setAutoClose(true);


        // listen to events happening in the Callable
        callable.addListener(new TaskListener() {

            // warning, the listener is called on another thread

            @Override
            public void onProgress(int current, int total, Object progressInfo) {
                taskMonitor.onProgress(current, total, progressInfo);
            }

            @Override
            public void onCompleted(Object completionInfo) {
                taskMonitor.onCompleted(completionInfo);
            }

            @Override
            public void onError(Exception e) {
                taskMonitor.onError(e);
            }
        });

        statusLayout.add(taskMonitor);

    }


    /**
     * Download indexes
     */
    private void downloadIndexes(){
        final MarketService.DownloadHandler[] handler = {null}; // use single-element array to avoid the need to be final

        // setup the progress dialog
        Text text = new Text("Downloading...");
        ProgressBar progressBar = new ProgressBar();
        progressBar.setIndeterminate(true);
        VerticalLayout layout = new VerticalLayout();
        layout.add(text, progressBar);
        Button bAbort = new Button();
        ConfirmDialog dialog = ConfirmDialog.create()
                .withMessage(layout)
                .withButton(bAbort, ButtonOption.caption("Abort"), ButtonOption.closeOnClick(false));
        dialog.setCloseOnEsc(false);
        dialog.setCloseOnOutsideClick(false);
        bAbort.addClickListener((ComponentEventListener<ClickEvent<Button>>) event1 -> {
            handler[0].setAbort(true);
        });
        dialog.setWidth("20em");
        dialog.open();

        UI ui = UI.getCurrent();
        int maxReqPerMinute = utils.toPrimitive(limitField.getValue());

        // download data in a separate thread
        new Thread(() -> {

            handler[0] =  marketService.downloadIndexes(new MarketService.DownloadListener() {
                @Override
                public void onDownloadCompleted() {
                    ui.access(new Command() {
                        @Override
                        public void execute() {
                            dialog.close();
                        }
                    });

                }

                @Override
                public void onDownloadAborted(Exception e) {
                    ui.access(new Command() {
                        @Override
                        public void execute() {
                            dialog.close();
                            ConfirmDialog dialog1 = ConfirmDialog.createError().withMessage("Download failed: "+e.getMessage());
                            dialog1.open();
                        }
                    });

                }

                @Override
                public void onDownloadProgress(int current, int total, String message) {

                    ui.access(new Command() {
                        @Override
                        public void execute() {
                            progressBar.setMax(total);
                            progressBar.setValue(current);
                            if(current==0){
                                progressBar.setIndeterminate(true);
                                text.setText(message);
                            }else{
                                progressBar.setIndeterminate(false);
                                text.setText("["+current+"/"+total+"] "+message);
                            }
                        }
                    });
                }

            },maxReqPerMinute);
        }).start();
    }



}


