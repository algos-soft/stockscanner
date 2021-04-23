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
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
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
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

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
