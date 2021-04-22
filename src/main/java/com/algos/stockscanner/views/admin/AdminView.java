package com.algos.stockscanner.views.admin;

import com.algos.stockscanner.beans.Utils;
import com.algos.stockscanner.data.entity.MarketIndex;
import com.algos.stockscanner.services.AdminService;
import com.algos.stockscanner.services.MarketService;
import com.algos.stockscanner.views.main.MainView;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.Command;
import org.claspina.confirmdialog.ButtonOption;
import org.claspina.confirmdialog.ConfirmDialog;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

@Route(value = "admin", layout = MainView.class)
@PageTitle("Admin")
@CssImport("./views/admin/admin-view.css")
public class AdminView extends VerticalLayout {

    private static final String MARKET_INDEXES = "Market Indexes";
    private static final String GENERATOR = "Generator";

    private Div placeholder;
    private Component marketIndexesComponent;
    private Component generatorComponent;

    private IntegerField limitField;

    private @Autowired
    Utils utils;

    @Autowired
    private MarketService marketService;

    public AdminView(AdminService adminService) {
        addClassName("admin-view");
    }

    @PostConstruct
    private void init() {

        Select<String> selector = new Select<>();
        selector.setItems(MARKET_INDEXES, GENERATOR);
        selector.setLabel("Topic");
        selector.addValueChangeListener(new HasValue.ValueChangeListener<AbstractField.ComponentValueChangeEvent<Select<String>, String>>() {
            @Override
            public void valueChanged(AbstractField.ComponentValueChangeEvent<Select<String>, String> event) {
                switch (event.getValue()) {
                    case MARKET_INDEXES:
                        placeholder.removeAll();
                        if(marketIndexesComponent==null){
                            marketIndexesComponent=buildMarketIndexesComponent();
                        }
                        placeholder.add(marketIndexesComponent);
                        break;
                    case GENERATOR:
                        placeholder.removeAll();
                        if(generatorComponent==null){
                            generatorComponent=buildGeneratorComponent();
                        }
                        placeholder.add(generatorComponent);
                        break;
                }
            }
        });

        placeholder = new Div();
        add(selector, placeholder);
    }


    private Component buildMarketIndexesComponent(){

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

        VerticalLayout layout = new VerticalLayout();
        //layout.getStyle().set("background","yellow");
        layout.add(layout1, bUpdateAllIndexData);

        return layout;

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
