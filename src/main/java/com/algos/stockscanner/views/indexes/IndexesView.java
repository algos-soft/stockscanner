package com.algos.stockscanner.views.indexes;

import com.algos.stockscanner.Application;
import com.algos.stockscanner.beans.Utils;
import com.algos.stockscanner.data.entity.MarketIndex;
import com.algos.stockscanner.data.entity.Simulation;
import com.algos.stockscanner.data.service.MarketIndexService;
import com.algos.stockscanner.enums.FrequencyTypes;
import com.algos.stockscanner.enums.IndexCategories;
import com.algos.stockscanner.enums.PriceUpdateModes;
import com.algos.stockscanner.exceptions.InvalidBigNumException;
import com.algos.stockscanner.services.AdminService;
import com.algos.stockscanner.services.DownloadIndexCallable;
import com.algos.stockscanner.services.MarketService;
import com.algos.stockscanner.services.UpdatePricesCallable;
import com.algos.stockscanner.task.TaskHandler;
import com.algos.stockscanner.task.TaskListener;
import com.algos.stockscanner.views.PageSubtitle;
import com.algos.stockscanner.views.main.MainView;
import com.algos.stockscanner.views.simulations.SimulationModel;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.IronIcon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.Command;
import com.vaadin.flow.server.StreamResource;
import org.apache.commons.lang3.StringUtils;
import org.claspina.confirmdialog.ButtonOption;
import org.claspina.confirmdialog.ConfirmDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Route(value = "indexes", layout = MainView.class)
@PageTitle(Application.APP_NAME + " | Indexes")
@PageSubtitle("Indexes")
@CssImport(value = "./views/indexes/indexes-view.css")
public class IndexesView extends Div {

    private static final Logger log = LoggerFactory.getLogger(IndexesView.class);

    private Grid<IndexModel> grid;

    private List<QuerySortOrder> order; // current order

    private FilterPanel filterPanel;

    private Label counterLabel;

    private Button anchorButton;

    private InputStream excelInputStream;

    @Autowired
    private Utils utils;

    @Autowired
    private MarketIndexService marketIndexService;

    @Autowired
    private MarketService marketService;

    @Autowired
    private AdminService adminService;


    @Autowired
    private ApplicationContext context;


    public IndexesView() {
    }


    @PostConstruct
    private void init() {

        addClassName("indexes-view");
        setSizeFull();

        filterPanel = context.getBean(FilterPanel.class);
        filterPanel.addListener(() -> grid.getDataProvider().refreshAll());

        counterLabel=new Label();

        createGrid();

        VerticalLayout layout = new VerticalLayout();
        layout.getStyle().set("height", "100%");
        layout.add(filterPanel, counterLabel, grid);

        add(layout);

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

        // Add to the UI an invisible button with anchor.
        // This button is clicked programmatically on the client to download the file
        // When this happens, the createResource() method is invoked which should
        // return the resource to download in the form of an InputStream.
        anchorButton = new Button();
        anchorButton.getStyle().set("display", "none");
        Anchor download = new Anchor(new StreamResource("indexes.xls", () -> excelInputStream), "");
        download.getElement().setAttribute("download", true);
        download.add(anchorButton);
        add(download);

    }

    private void customizeHeader(HorizontalLayout header) {

        header.getStyle().set("flex-direction", "row-reverse");

        Button addButton = new Button("New Index", new Icon(VaadinIcon.PLUS_CIRCLE));
        addButton.getStyle().set("margin-left", "1em");
        addButton.getStyle().set("margin-right", "1em");
        addButton.setIconAfterText(true);
        addButton.addClickListener((ComponentEventListener<ClickEvent<Button>>) buttonClickEvent -> {
            addNewItem();
        });


        Button expButton = new Button("Export", new Icon(VaadinIcon.ARROW_CIRCLE_DOWN_O));
        expButton.getStyle().set("margin-left", "1em");
        expButton.getStyle().set("margin-right", "1em");
        expButton.setIconAfterText(true);
        expButton.addClickListener((ComponentEventListener<ClickEvent<Button>>) buttonClickEvent -> {
            byte[] barray = marketIndexService.exportExcel(grid.getDataProvider());
            if(barray!=null){
                excelInputStream = new ByteArrayInputStream(barray);
                anchorButton.clickInClient();
            }
        });

        header.add(addButton, expButton);

    }



    private void createGrid(){

        grid = new Grid<>();
        grid.setHeight("100%");
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_NO_ROW_BORDERS);
        grid.addComponentColumn(index -> createCard(index));

        CallbackDataProvider<IndexModel, Void> provider;
        provider = DataProvider.fromCallbacks(fetchCallback -> {
            try {
                IndexFilter indexFilter = filterPanel.buildFilter();
                int offset = fetchCallback.getOffset();
                int limit = fetchCallback.getLimit();
                order = fetchCallback.getSortOrders();
                List<MarketIndex> entities = marketIndexService.fetch(offset, limit, indexFilter, order);
                return marketIndexService.entitiesToModels(entities).stream();
            } catch (InvalidBigNumException e) {
                log.warn(e.getMessage());
                return null;
            }
        }, countCallback -> {
            try {
                IndexFilter indexFilter = filterPanel.buildFilter();
                int count=marketIndexService.count(indexFilter);
                counterLabel.setText(count+" rows");
                return count;
            } catch (InvalidBigNumException e) {
                log.warn(e.getMessage());
                return 0;
            }
        });

        grid.setDataProvider(provider);

    }





    /**
     * Present an empty dialog to create a new item
     */
    private void addNewItem() {

        IndexDialogConfirmListener listener = new IndexDialogConfirmListener() {
            @Override
            public void onConfirm(IndexModel model) {
                MarketIndex entity = new MarketIndex();
                marketIndexService.modelToEntity(model, entity);
                marketIndexService.update(entity);
                grid.getDataProvider().refreshAll();
            }
        };

        IndexDialog dialog = context.getBean(IndexDialog.class, listener);

        dialog.open();
    }


    private HorizontalLayout createCard(IndexModel model) {

        HorizontalLayout card = new HorizontalLayout();
        card.addClassName("card");
        card.setSpacing(false);
        card.getThemeList().add("spacing-s");

        Component pan1 = buildPan1(model);
        Component pan2 = buildPan2(model);
        Component pan3 = buildPan3(model);
        Component pan4 = buildPan4(model);
        Component action = buildActionCombo(model);

        card.add(pan1, pan2, pan3, pan4, action);

        return card;
    }


    private Component buildPan1(IndexModel model) {
        Pan pan = new Pan();
        Image image = utils.byteArrayToImage(model.getImageData());
        pan.add(image);
        pan.addClassName("indexesview-pan1");
        return pan;
    }


    private Component buildPan2(IndexModel model) {

        Span symbol = new Span(model.getSymbol());
        symbol.addClassName("symbol");

        Span name = new Span(model.getName());
        name.addClassName("name");

        String categoryDesc = null;
        IndexCategories indexCategory = model.getCategory();
        if (indexCategory != null) {
            categoryDesc = indexCategory.getDescription();
        }
        Span category = new Span(categoryDesc);
        category.addClassName("category");

        Pan pan = new Pan();
        pan.addClassName("indexesview-pan2");
        pan.add(symbol, name, category);

        return pan;
    }


    private Component buildPan3(IndexModel model) {

        IronIcon intervalIcon = new IronIcon("vaadin", "line-chart");
        intervalIcon.addClassName("indexes-view-icon");
        String text;
        if (model.getNumUnits() > 0) {
            text = format(model.getUnitsFrom()) + " -> " + format(model.getUnitsTo());
        } else {
            text = "no data";
        }
        Span intervalSpan = new Span(text);
        intervalSpan.addClassName("interval");
        HorizontalLayout row1 = new HorizontalLayout();
        row1.addClassName("details");
        row1.add(intervalIcon, intervalSpan);


        IronIcon pointsIcon = new IronIcon("vaadin", "ellipsis-dots-h");
        pointsIcon.addClassName("indexes-view-icon");
        Span pointsSpan = new Span(String.format("%,d", model.getNumUnits()));
        pointsSpan.addClassName("points");


        IronIcon frequencyIcon = new IronIcon("vaadin", "clock");
        frequencyIcon.addClassName("indexes-view-icon");
        String frequencyDesc = null;
        FrequencyTypes frequencyType = model.getUnitFrequency();
        if (frequencyType != null) {
            frequencyDesc = frequencyType.getDescription();
        }


        Span frequencySpan = new Span(frequencyDesc);
        frequencySpan.addClassName("frequency");
//        frequencySpan.getStyle().set("margin-left","0em");
//        HorizontalLayout row3 = new HorizontalLayout();
//        row3.addClassName("details");
//        row3.setVisible(model.getNumUnits() > 0);
//        row3.add(frequencyIcon, frequencySpan);

        HorizontalLayout row2 = new HorizontalLayout();
        row2.addClassName("details");
        row2.setVisible(model.getNumUnits() > 0);
        row2.add(pointsIcon, pointsSpan, frequencyIcon, frequencySpan);


        IronIcon icon1 = new IronIcon("vaadin", "clock");
        icon1.addClassName("indexes-view-icon");
        String text1;
        if (model.getFundamentalUpdateTs() != null) {
            text1 = formatTs(model.getFundamentalUpdateTs());
        } else {
            text1 = "never";
        }
        text1 = "idx upd: " + text1;
        Span span1 = new Span(text1);
        span1.addClassName("interval");

        HorizontalLayout row4 = new HorizontalLayout();
        row4.addClassName("details");
        row4.add(icon1, span1);

        IronIcon icon2 = new IronIcon("vaadin", "clock");
        icon2.addClassName("indexes-view-icon");
        String text2;
        if (model.getPricesUpdateTs() != null) {
            text2 = formatTs(model.getPricesUpdateTs());
        } else {
            text2 = "never";
        }
        text2 = "price upd: " + text2;
        Span span2 = new Span(text2);
        span2.addClassName("interval");
        HorizontalLayout row5 = new HorizontalLayout();
        row5.addClassName("details");
        row5.add(icon2, span2);


        Pan pan = new Pan();
        pan.addClassName("indexesview-pan3");
        pan.add(row1, row2, row4, row5);

        return pan;
    }


    private Component buildPan4(IndexModel model) {

        Span span1 = new Span(model.getExchange() + ", " + model.getCountry());
        span1.addClassName("detail-row");
        Span span2 = new Span(model.getSector() + ", " + model.getIndustry());
        span2.addClassName("detail-row");
        Span span3 = new Span("cap: " + utils.numberWithSuffix(model.getMarketCap()) + ", ebitda: " + utils.numberWithSuffix(model.getEbitda()));
        span3.addClassName("detail-row");

        Pan pan = new Pan();
        pan.addClassName("indexesview-pan4");
        pan.add(span1, span2, span3);

        return pan;
    }


    private String format(LocalDate d) {
        if (d != null) {
            return d.format(DateTimeFormatter.ofPattern("dd MMM u"));
        }

        return null;
    }

    private String formatTs(LocalDateTime ts) {
        if (ts != null) {
            return ts.format(DateTimeFormatter.ofPattern("dd MMM u kk:mm"));
        }

        return null;
    }


    private Component buildActionCombo(IndexModel model) {

        MenuBar menuBar = new MenuBar();
        MenuItem account = menuBar.addItem("Actions...");


        // edit an item
        account.getSubMenu().addItem("Edit index", i -> {

            MarketIndex entity = marketIndexService.get(model.getId()).get();

            IndexDialogConfirmListener listener = model1 -> {
                marketIndexService.modelToEntity(model1, entity);
                marketIndexService.update(entity);   // write db
                marketIndexService.entityToModel(entity, model1); // from db back to model - to be sure model is aligned with db
                grid.getDataProvider().refreshItem(model1); // refresh only this item
            };

            IndexDialog dialog = context.getBean(IndexDialog.class, model, listener);

            dialog.open();

        });

        // Delete an Index
        account.getSubMenu().addItem("Delete index", i -> {

            Button bConfirm = new Button();
            ConfirmDialog dialog = ConfirmDialog.create().withMessage("Do you want to delete " + model.getSymbol() + "?")
                    .withButton(new Button(), ButtonOption.caption("Cancel"), ButtonOption.closeOnClick(true))
                    .withButton(bConfirm, ButtonOption.caption("Delete"), ButtonOption.focus(), ButtonOption.closeOnClick(true));

            bConfirm.addClickListener((ComponentEventListener<ClickEvent<Button>>) event1 -> {
                try {
                    marketIndexService.delete(model.getId());
                    grid.getDataProvider().refreshAll();
                } catch (Exception e) {
                    log.error("could not delete index entity id " + model.getId(), e);
                }
            });

            dialog.open();
        });


        // Update info
        account.getSubMenu().addItem("Update info", i -> {

            List<String> symbols = new ArrayList<>();
            symbols.add(model.getSymbol());
            DownloadIndexCallable callable = adminService.scheduleDownload(symbols, 5);
            TaskHandler handler = callable.obtainHandler();

            // setup the progress dialog
            Text text = new Text("Loading...");
            ProgressBar progressBar = new ProgressBar();
            progressBar.setIndeterminate(true);
            VerticalLayout layout = new VerticalLayout();
            layout.add(text, progressBar);
            Button bAbort = new Button();
            ConfirmDialog dialog = ConfirmDialog.create()
                    .withCaption("Updating info")
                    .withMessage(layout)
                    .withButton(bAbort, ButtonOption.caption("Abort"), ButtonOption.closeOnClick(false));
            dialog.setCloseOnEsc(false);
            dialog.setCloseOnOutsideClick(false);
            bAbort.addClickListener((ComponentEventListener<ClickEvent<Button>>) event1 -> {
                handler.abort();
            });
            dialog.setWidth("20em");
            dialog.open();

            // attach a listener to the task
            UI ui = UI.getCurrent();
            callable.addListener(new TaskListener() {
                @Override
                public void onStarted(Object info) {
                }

                @Override
                public void onProgress(int current, int total, Object info) {

                    ui.access(new Command() {
                        @Override
                        public void execute() {
                            progressBar.setMax(total);
                            progressBar.setValue(current);

                            String message="";
                            if(info!=null){
                                message=info.toString();
                            }
                            if (current == 0) {
                                progressBar.setIndeterminate(true);
                                text.setText(message);
                            } else {
                                progressBar.setIndeterminate(false);
                                text.setText(message + ": " + current + "/" + total);
                            }
                        }
                    });

                }

                @Override
                public void onCompleted(Object info) {

                    ui.access(new Command() {
                        @Override
                        public void execute() {
                            dialog.close();

                            MarketIndex entity = marketIndexService.get(model.getId()).get();
                            marketIndexService.entityToModel(entity, model);
                            grid.getDataProvider().refreshItem(model);

                        }
                    });

                }

                @Override
                public void onError(Exception e) {
                    ui.access(new Command() {
                        @Override
                        public void execute() {
                            dialog.close();
                            ConfirmDialog dialog1 = ConfirmDialog.createError().withMessage("Download failed: " + e.getMessage());
                            dialog1.open();
                        }
                    });

                }
            });
        });



        // Update prices
        account.getSubMenu().addItem("Update prices", i -> {

            List<String> symbols = new ArrayList<>();
            symbols.add(model.getSymbol());
            UpdatePricesCallable callable = adminService.scheduleUpdate(symbols, PriceUpdateModes.ADD_MISSING_DATA_ONLY, 5);
            TaskHandler handler = callable.obtainHandler();

            // setup the progress dialog
            Text text = new Text("Loading...");
            ProgressBar progressBar = new ProgressBar();
            progressBar.setIndeterminate(true);
            VerticalLayout layout = new VerticalLayout();
            layout.add(text, progressBar);
            Button bAbort = new Button();
            ConfirmDialog dialog = ConfirmDialog.create()
                    .withCaption("Updating prices")
                    .withMessage(layout)
                    .withButton(bAbort, ButtonOption.caption("Abort"), ButtonOption.closeOnClick(false));
            dialog.setCloseOnEsc(false);
            dialog.setCloseOnOutsideClick(false);
            bAbort.addClickListener((ComponentEventListener<ClickEvent<Button>>) event1 -> {
                handler.abort();
            });
            dialog.setWidth("20em");
            dialog.open();

            // attach a listener to the task
            UI ui = UI.getCurrent();
            callable.addListener(new TaskListener() {
                @Override
                public void onStarted(Object info) {
                }

                @Override
                public void onProgress(int current, int total, Object info) {

                    ui.access(new Command() {
                            @Override
                            public void execute() {
                                progressBar.setMax(total);
                                progressBar.setValue(current);

                                String message="";
                                if(info!=null){
                                    message=info.toString();
                                }
                                if (current == 0) {
                                    progressBar.setIndeterminate(true);
                                    text.setText(message);
                                } else {
                                    progressBar.setIndeterminate(false);
                                    text.setText(message + ": " + current + "/" + total);
                                }
                            }
                        });

                }

                @Override
                public void onCompleted(Object info) {

                    ui.access(new Command() {
                        @Override
                        public void execute() {
                            dialog.close();

                            MarketIndex entity = marketIndexService.get(model.getId()).get();
                            marketIndexService.entityToModel(entity, model);
                            grid.getDataProvider().refreshItem(model);

                        }
                    });

                }

                @Override
                public void onError(Exception e) {
                    ui.access(new Command() {
                        @Override
                        public void execute() {
                            dialog.close();
                            ConfirmDialog dialog1 = ConfirmDialog.createError().withMessage("Download failed: " + e.getMessage());
                            dialog1.open();
                        }
                    });

                }
            });
        });


        return menuBar;

    }






    /**
     * Transform Entity to view Model
     */
    private IndexModel createIndex(MarketIndex index) {
        IndexModel m = new IndexModel();
        marketIndexService.entityToModel(index, m);
        return m;
    }


    /**
     * base for card panels
     */
    class Pan extends VerticalLayout {
        public Pan() {
            setSpacing(false);
            setPadding(false);
            getThemeList().add("spacing-s");
            addClassName("panel");
        }
    }


}
