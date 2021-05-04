package com.algos.stockscanner.views.admin;

import com.algos.stockscanner.beans.Utils;
import com.algos.stockscanner.data.entity.MarketIndex;
import com.algos.stockscanner.data.service.MarketIndexService;
import com.algos.stockscanner.task.TaskHandler;
import com.algos.stockscanner.task.TaskListener;
import com.algos.stockscanner.services.*;
import com.algos.stockscanner.task.TaskMonitor;
import com.algos.stockscanner.views.main.MainView;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.Command;
import org.claspina.confirmdialog.ButtonOption;
import org.claspina.confirmdialog.ConfirmDialog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;

@Route(value = "admin", layout = MainView.class)
@PageTitle("Admin")
@CssImport("./views/admin/admin-view.css")
public class AdminView extends VerticalLayout {

    private static final String MARKET_INDEXES = "Market Indexes";
    private static final String GENERATOR = "Generator";

    private Component marketIndexesComponent;
    private Component generatorComponent;

    private IntegerField limitField;


    private @Autowired
    Utils utils;

    @Autowired
    private MarketService marketService;

    @Autowired
    private AdminService adminService;

    @Autowired
    private MarketIndexService marketIndexService;

    @Autowired
    private ApplicationContext context;

    public AdminView(AdminService adminService) {
        addClassName("admin-view");
    }

    @PostConstruct
    private void init() {

        // customize the header
        addAttachListener((ComponentEventListener<AttachEvent>) attachEvent -> {
            Optional<Component> parent = getParent();
            if (parent.isPresent()) {
                Optional<HorizontalLayout> customArea = utils.findCustomArea(parent.get());
                if (customArea.isPresent()) {
                    customArea.get().removeAll();
                    customizeHeader(customArea.get());
                }
            }
        });

    }



    private void customizeHeader(HorizontalLayout header) {

        String bWidth="11em";

        header.getStyle().set("flex-direction", "row-reverse");

        Button button1 = new Button(MARKET_INDEXES, new Icon(VaadinIcon.LINE_BAR_CHART));
        button1.getStyle().set("margin-left", "0.5em");
        button1.getStyle().set("margin-right", "0.5em");
        button1.getStyle().set("width", bWidth);
        button1.setIconAfterText(true);
        button1.addClickListener((ComponentEventListener<ClickEvent<Button>>) buttonClickEvent -> {
            removeAll();
            if(marketIndexesComponent==null){
                marketIndexesComponent=buildMarketIndexesComponent();
            }
            add(marketIndexesComponent);
        });


        Button button2 = new Button(GENERATOR, new Icon(VaadinIcon.COG_O));
        button2.getStyle().set("margin-left", "0.5em");
        button2.getStyle().set("margin-right", "0.5em");
        button2.getStyle().set("width", bWidth);
        button2.setIconAfterText(true);
        button2.addClickListener((ComponentEventListener<ClickEvent<Button>>) buttonClickEvent -> {
            removeAll();
            if(generatorComponent==null){
                generatorComponent=buildGeneratorComponent();
            }
            add(generatorComponent);
        });


        header.add(button2, button1);
    }



    private Component buildMarketIndexesComponent(){

        HorizontalLayout statusLayout = new HorizontalLayout();

        Button bDownloadIndexes = new Button("Download indexes");
        bDownloadIndexes.addClickListener(new ComponentEventListener<ClickEvent<Button>>() {
            @Override
            public void onComponentEvent(ClickEvent<Button> buttonClickEvent) {
                downloadIndexes();
            }
        });
        limitField=new IntegerField("Max req per minute");
        limitField.setMinWidth("9em");
        limitField.setValue(5);
        HorizontalLayout layout1=new HorizontalLayout();
        layout1.setAlignItems(Alignment.BASELINE);
        layout1.add(bDownloadIndexes, limitField);


        Button bUpdateAllIndexData = new Button("Update data for all indexes");
        bUpdateAllIndexData.addClickListener(new ComponentEventListener<ClickEvent<Button>>() {
            @Override
            public void onComponentEvent(ClickEvent<Button> buttonClickEvent) {
                try {


                    TaskHandler handler=new TaskHandler();

                    // setup the progress dialog
                    Text text = new Text("Loading data...");
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
                        handler.abort();
                    });
                    dialog.setWidth("20em");

                    // keep this out of the listener, the listener is called on another thread
                    UI ui = UI.getCurrent();

                    TaskListener listener = new TaskListener() {
                        @Override
                        public void onProgress(int current, int total, Object info) {
                            ui.access((Command) () -> {
                                progressBar.setIndeterminate(false);
                                progressBar.setValue(current);
                                progressBar.setMax(total);
                            });
                        }

                        @Override
                        public void onCompleted(boolean aborted) {
                            ui.access((Command) () -> dialog.close());
                        }

                        @Override
                        public void onError(Exception e) {
                            e.printStackTrace();
                            ui.access((Command) () -> dialog.close());
                        }
                    };

                    dialog.open();

                    // start background operations
                    MarketIndex index = marketIndexService.findUniqueBySymbol("AAPL");
                    Future future = adminService.downloadIndexData(index, "ALL", null, listener, handler);
                    //Object obj = future.get();
                    int a = 87;
                    int b = a;

//                    List<MarketIndex> indexes = marketIndexService.findAll();
//                    for(MarketIndex index : indexes){
//                        adminService.downloadIndexData(index, "ALL", null, listener, handler);
//                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


        Button bTest = new Button("Test task scheduler");
        bTest.addClickListener(new ComponentEventListener<ClickEvent<Button>>() {
            @Override
            public void onComponentEvent(ClickEvent<Button> buttonClickEvent) {
                try {
                    List<MarketIndex> indexes = new ArrayList<>();
                    indexes.add(marketIndexService.findUniqueBySymbol("AAPL"));
                    indexes.add(marketIndexService.findUniqueBySymbol("AMZN"));
                    indexes.add(marketIndexService.findUniqueBySymbol("PYPL"));

                    List<UpdateIndexDataCallable> callables = adminService.scheduleUpdate(indexes, 5);

                    // keep this out of the listener, the listener is called on another thread
                    UI ui = UI.getCurrent();

                    for(UpdateIndexDataCallable callable : callables){

                        // GUI component handling events coming from the monitor
                        TaskMonitor taskMonitor = context.getBean(TaskMonitor.class, ui, null);
                        TaskMonitor.MonitorListener listener = new TaskMonitor.MonitorListener() {
                            @Override
                            public void onAborted() {
                                callable.getHandler().abort();
                            }

                            @Override
                            public void onClosed() {
                                ui.access((Command) () -> statusLayout.remove(taskMonitor));
                            }
                        };
                        taskMonitor.setMonitorListener(listener);


                        // listen to events happening in the Callable
                        callable.setListener(new TaskListener() {
                            @Override
                            public void onProgress(int current, int total, Object info) {
                                taskMonitor.onProgress(current, total, info);
                            }

                            @Override
                            public void onCompleted(boolean aborted) {
                                taskMonitor.onCompleted(aborted);
                            }

                            @Override
                            public void onError(Exception e) {
                                taskMonitor.onError(e);
                            }
                        });

                        callable.setHandler(new TaskHandler());

                        statusLayout.add(taskMonitor);

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });


        VerticalLayout content = new VerticalLayout();
        content.add(layout1, bUpdateAllIndexData, bTest);

        VerticalLayout page = new VerticalLayout();
        page.add(content, statusLayout);

        return page;

    }


    private Component buildGeneratorComponent(){

        Button bDownloadIndexes = new Button("Test");

        VerticalLayout layout = new VerticalLayout();
        //layout.getStyle().set("background","yellow");
        layout.add(bDownloadIndexes);

        return layout;

    }



    /**
     * Download indexes
     */
    public void downloadIndexes(){
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
