package com.algos.stockscanner.views.generators;

import com.algos.stockscanner.Application;
import com.algos.stockscanner.beans.ContextStore;
import com.algos.stockscanner.beans.Utils;
import com.algos.stockscanner.data.entity.Generator;
import com.algos.stockscanner.data.entity.MarketIndex;
import com.algos.stockscanner.data.service.GeneratorService;
import com.algos.stockscanner.data.service.MarketIndexService;
import com.algos.stockscanner.data.service.SimulationService;
import com.algos.stockscanner.runner.RunnerService;
import com.algos.stockscanner.services.SimulationCallable;
import com.algos.stockscanner.task.TaskHandler;
import com.algos.stockscanner.task.TaskListener;
import com.algos.stockscanner.task.TaskMonitor;
import com.algos.stockscanner.views.PageSubtitle;
import com.algos.stockscanner.views.indexes.IndexModel;
import com.algos.stockscanner.views.simulations.SimulationsView;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.IronIcon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.router.*;
import com.algos.stockscanner.views.main.MainView;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.server.Command;
import org.claspina.confirmdialog.ButtonOption;
import org.claspina.confirmdialog.ConfirmDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Example;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Route(value = "generators", layout = MainView.class)
@RouteAlias(value = "", layout = MainView.class)
@PageTitle(Application.APP_NAME +" | Generators")
@PageSubtitle("Generators")
@CssImport(value = "./views/generators/generators-view.css")
@CssImport(value = "./views/generators/generators-grid.css", themeFor = "vaadin-grid")
public class GeneratorsView extends Div implements AfterNavigationObserver {

    private static final Logger log = LoggerFactory.getLogger(GeneratorsView.class);

    private static final String RUNNERS_KEY = "runners";

    private Grid<GeneratorModel> grid;

    private String filtSymbol;

    private Example<Generator> filter;

    private HorizontalLayout statusLayout;


    @Autowired
    private GeneratorService generatorService;

    @Autowired
    private MarketIndexService marketIndexService;

    @Autowired
    private SimulationService simulationService;

    @Autowired
    private RunnerService runnerService;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private Utils utils;

    @Autowired
    private ContextStore contextStore;


    public GeneratorsView() {
    }


    @PostConstruct
    private void init() {

        addClassName("generators-view");
        setSizeFull();

        filter = Example.of(new Generator());    // empty initial filter

        createGrid();
        //Component filterPanel = createFilterPanel();

        statusLayout = new HorizontalLayout();
        statusLayout.setSpacing(false);
        statusLayout.setPadding(false);
        statusLayout.addClassName("generators-view-statuslayout");


        VerticalLayout layout = new VerticalLayout();
        layout.getStyle().set("height", "100%");
//        layout.add(filterPanel, grid, statusLayout);
        layout.add(grid, statusLayout);

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

//        // retrieve the runners from the context and put them in the UI
//        List<GeneratorRunner> runners = getContextRunners();
//        for (GeneratorRunner runner : runners) {
//            runner.getElement().removeFromTree();   // remove if attached to previous tree
//            addRunnerToUI(runner);
//        }

        // retrieve the running tasks from the context, create Task Monitors and put them in the UI
        Collection<SimulationCallable> callables = contextStore.simulationCallableMap.values();
        for(SimulationCallable callable : callables){
            attachMonitorToTask(callable);
        }


    }


    /**
     * Reload data when this view is displayed.
     */
    @Override
    public void afterNavigation(AfterNavigationEvent event) {
    }

    private void customizeHeader(HorizontalLayout header) {

        header.getStyle().set("flex-direction", "row-reverse");

        Button addButton = new Button("New Generator", new Icon(VaadinIcon.PLUS_CIRCLE));
        addButton.getStyle().set("margin-left", "1em");
        addButton.getStyle().set("margin-right", "1em");
        addButton.setIconAfterText(true);
        addButton.addClickListener((ComponentEventListener<ClickEvent<Button>>) buttonClickEvent -> {
            addNewItem();
        });

        header.add(addButton);
    }


    private void createGrid() {
        grid = new Grid<>();
        grid.setHeight("100%");
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_NO_ROW_BORDERS);
        grid.addComponentColumn(index -> createCard(index));

        CallbackDataProvider<GeneratorModel, Void> provider;
        provider = DataProvider.fromCallbacks(fetchCallback -> {
            int offset = fetchCallback.getOffset();
            int limit = fetchCallback.getLimit();
            List<QuerySortOrder> sorts = fetchCallback.getSortOrders();
            return generatorService.fetch(offset, limit, filter, sorts).stream();
        }, countCallback -> {
            return generatorService.count(filter);
        });

        grid.setDataProvider(provider);


    }

    private Component createFilterPanel() {

        ComboBox<MarketIndex> indexCombo = utils.buildIndexCombo();
        indexCombo.addValueChangeListener(new HasValue.ValueChangeListener<AbstractField.ComponentValueChangeEvent<ComboBox<MarketIndex>, MarketIndex>>() {
            @Override
            public void valueChanged(AbstractField.ComponentValueChangeEvent<ComboBox<MarketIndex>, MarketIndex> event) {
                filtSymbol = null;
                MarketIndex marketIndex = event.getValue();
                if (marketIndex != null) {
                    filtSymbol = marketIndex.getSymbol();
                }
                filter();
            }
        });

        HorizontalLayout layout = new HorizontalLayout();
        layout.add(indexCombo);
        return layout;
    }


    private HorizontalLayout createCard(GeneratorModel model) {

        HorizontalLayout card = new HorizontalLayout();
        card.addClassName("card");
        card.setSpacing(false);
        card.getThemeList().add("spacing-s");

        Component pan0 = buildPan0(model);
        Component pan1 = buildPan1(model);
        Component pan2 = buildPan2(model);
        Component pan3 = buildPan3(model);
        Component pan4 = buildPan4(model);
        Component action = buildActionCombo(model);

        card.add(pan0, pan1, pan2, pan3, pan4, action);

        return card;
    }


    private Component buildPan0(GeneratorModel model) {
        IronIcon tagIcon = new IronIcon("vaadin", "tag");
        int number = utils.toPrimitive(model.getNumber());
        Span sNumber = new Span("" + number);
        sNumber.addClassName("number");
        HorizontalLayout row1 = new HorizontalLayout();
        row1.addClassName("tagRow");
        row1.add(tagIcon, sNumber);

        Div nameDiv = new Div();
        nameDiv.addClassName("genview-name");
        nameDiv.setText(model.getName());

        Pan pan = new Pan();
        pan.setMaxWidth("6em");
        pan.add(row1, nameDiv);
        return pan;
    }

    private Component buildPan1(GeneratorModel model) {

        Component comp;

        if (model.isPermutateIndexes()) {
            IndexesPanel panel = context.getBean(IndexesPanel.class, "-list");
            for (IndexModel idxModel : model.getIndexes()) {
                IndexComponent idxComp = context.getBean(IndexComponent.class, idxModel.getId(), idxModel.getImageData(), idxModel.getSymbol(), "-list");
                panel.add(idxComp);
            }
            comp = panel;
        } else {
            Image img = model.getImage();
            if (img == null) {
                img=utils.getDefaultIndexIcon();
            }
            img.addClassName("icon");

            Span symbol = new Span(model.getSymbol());
            symbol.addClassName("symbol");
            HorizontalLayout hl = new HorizontalLayout();
            hl.add(img, symbol);
            comp = hl;
        }

        Pan pan = new Pan();
        pan.setMinWidth("20em");
        pan.setMaxWidth("20em");
        pan.add(comp);
        return pan;
    }


    private Component buildPan2(GeneratorModel model) {

        int amount = utils.toPrimitive(model.getAmount());
        Span cAmount = new Span(format(amount));
        cAmount.addClassName("amount");
        HorizontalLayout row1 = new HorizontalLayout();
        row1.add(cAmount);

        int sl = utils.toPrimitive(model.getStopLoss());
        Span cSL = new Span();
        if (sl > 0) {
            cSL.add("SL " + format(sl) + "%");
        }
        int tp = utils.toPrimitive(model.getTakeProfit());
        Span cTP = new Span();
        if (tp > 0) {
            cTP.add("TP " + format(tp) + "%");
        }
        HorizontalLayout row2 = new HorizontalLayout();
        row2.addClassName("sltp");
        row2.add(cSL, cTP);

        Pan pan = new Pan();
        pan.add(row1, row2);

        return pan;
    }


    private Component buildPan3(GeneratorModel model) {

        IronIcon calendar = new IronIcon("vaadin", "flag-checkered");
        String sDate;
        if (model.getStartDate() != null) {
            sDate = format(model.getStartDate());
        } else {
            sDate = "n.a.";
        }
        Span spanDate = new Span(calendar, new Text(sDate));
        spanDate.addClassName("startdate");

        String period;
        IronIcon durationIcon = new IronIcon("vaadin", "clock");
        if (model.getDays() > 0) {
            period = "max " + model.getDays() + " days";
        } else {
            period = "unlimited";
        }
        Span spanPeriod = new Span(durationIcon, new Text(period));
        spanPeriod.addClassName("period");

        String sSpan;
        IronIcon spanIcon = new IronIcon("vaadin", "refresh");
        if (model.getSpans() > 1) {
            sSpan = model.getSpans() + " spans";
        } else {
            sSpan = "single span";
        }
        Span spanSpan = new Span(spanIcon, new Span(sSpan));
        spanSpan.addClassName("spans");

        Pan pan = new Pan();
        pan.add(spanDate, spanPeriod, spanSpan);
        return pan;
    }


    private Component buildPan4(GeneratorModel model) {

        IronIcon amplIcon = new IronIcon("vaadin", "arrows-long-v");
        String sAmplitude;
        if (model.isPermutateAmpitude()) {
            sAmplitude = model.getAmplitudeMin() + "% - " + model.getAmplitudeMax() + "%, in " + model.getAmplitudeSteps() + " steps";
        } else {
            sAmplitude = model.getAmplitude() + "%";
        }
        Span spanAmplitude = new Span(amplIcon, new Text(sAmplitude));
        spanAmplitude.addClassName("amplitude");

        IronIcon lookIcon = new IronIcon("vaadin", "glasses");
        String sLook;
        if (model.isPermutateDaysLookback()) {
            sLook = model.getDaysLookbackMin() + " - " + model.getDaysLookbackMax() + " days, in " + model.getDaysLookbackSteps() + " steps";
        } else {
            sLook = model.getDaysLookback() + " days";
        }
        Span spanLook = new Span(lookIcon, new Text(sLook));
        spanLook.addClassName("lookback");

        Pan pan = new Pan();
        pan.add(spanAmplitude, spanLook);
        return pan;

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

    private String format(LocalDate d) {
        if (d != null) {
            return d.format(DateTimeFormatter.ofPattern("dd MMM u"));
        }
        return null;
    }

    private String format(Integer n) {
        if (n != null) {
            return String.format("%,d", n);
        }
        return null;
    }


    /**
     * Present an empty dialog to create a new item
     */
    private void addNewItem() {

        GeneratorDialogConfirmListener listener = new GeneratorDialogConfirmListener() {
            @Override
            public void onConfirm(GeneratorModel model) {
                Generator entity = new Generator();
                generatorService.initEntity(entity);
                generatorService.modelToEntity(model, entity);
                generatorService.update(entity);
                refreshGrid();
            }
        };

        GeneratorModel model = new GeneratorModel();
        generatorService.initModel(model);  // set defaults
        GeneratorDialog dialog = context.getBean(GeneratorDialog.class, model, listener);

        dialog.open();
    }


    /**
     * Transform Entity to view Model
     */
    private GeneratorModel createModel(Generator entity) {
        GeneratorModel m = new GeneratorModel();
        generatorService.entityToModel(entity, m);
        return m;
    }


    private Component buildActionCombo(GeneratorModel model) {

        MenuBar menuBar = new MenuBar();
        MenuItem account = menuBar.addItem("Actions...");


        // run an item
        account.getSubMenu().addItem("Run generator", i -> {
            run1(model);
        });

        // show results
        account.getSubMenu().addItem("Show results", i -> {
            UI.getCurrent().navigate(SimulationsView.class, "" + model.getNumber());
        });

        // edit an item
        account.getSubMenu().addItem("Edit generator", i -> {

            Generator entity = generatorService.get(model.getId()).get();

            GeneratorDialogConfirmListener listener = model1 -> {
                generatorService.modelToEntity(model1, entity);
                generatorService.update(entity);    // write db
                log.info("Generator id "+entity.getId()+" modified");
                log.debug("Generator data: "+entity);
                generatorService.entityToModel(entity, model1); // from db back to model - to be sure the model reflects the changes happened on db
                grid.getDataProvider().refreshItem(model1); // refresh only this item
            };

            GeneratorDialog dialog = context.getBean(GeneratorDialog.class, model, listener);

            dialog.open();

        });

        // Delete an Index
        account.getSubMenu().addItem("Delete generator", i -> {

            Button bConfirm = new Button();
            ConfirmDialog dialog = ConfirmDialog.create().withMessage("Do you want to delete " + model.getNumber() + " - " + model.getSymbol() + "?")
                    .withButton(new Button(), ButtonOption.caption("Cancel"), ButtonOption.closeOnClick(true))
                    .withButton(bConfirm, ButtonOption.caption("Delete"), ButtonOption.focus(), ButtonOption.closeOnClick(true));

            bConfirm.addClickListener((ComponentEventListener<ClickEvent<Button>>) event1 -> {
                try {
                    generatorService.delete(model.getId());
                    refreshGrid();
                } catch (Exception e) {
                    log.error("could not delete entity id "+model.getId(), e);
                }
            });

            dialog.open();
        });

        // edit an item
        account.getSubMenu().addItem("Clone generator", i -> {
        });

        // delete existing simulations
        account.getSubMenu().addItem("Delete simulations", i -> {

            Generator generator = generatorService.get(model.getId()).get();
            if (generator.getSimulations().size() > 0) {

                Button bConfirm = new Button();
                ConfirmDialog dialog = ConfirmDialog.create().withMessage("Do you want to delete " + generator.getSimulations().size() + " simulations?")
                        .withButton(new Button(), ButtonOption.caption("Cancel"), ButtonOption.closeOnClick(true))
                        .withButton(bConfirm, ButtonOption.caption("Delete"), ButtonOption.focus(), ButtonOption.closeOnClick(true));

                bConfirm.addClickListener((ComponentEventListener<ClickEvent<Button>>) event1 -> {
                    try {
                        simulationService.deleteBy(generator);
                    } catch (Exception e) {
                        log.error("could not delete simulations for generator id "+generator.getId(), e);
                    }
                });

                dialog.open();

            } else {
                ConfirmDialog dialog = ConfirmDialog.create().withMessage("No simulations found");
                dialog.open();
            }

        });


        return menuBar;

    }

    // run generator - phase 1
    private void run1(GeneratorModel model) {
        // check if there are previous simulations that will be deleted
        Generator generator = generatorService.get(model.getId()).get();
        int count = simulationService.countBy(generator);
        if (count > 0) {
            Button bConfirm = new Button();
            ConfirmDialog dialog = ConfirmDialog.create().withMessage(count + " previous simulations found, will be deleted.")
                    .withButton(new Button(), ButtonOption.caption("Cancel"), ButtonOption.closeOnClick(true))
                    .withButton(bConfirm, ButtonOption.caption("Continue"), ButtonOption.focus(), ButtonOption.closeOnClick(true));
            bConfirm.addClickListener((ComponentEventListener<ClickEvent<Button>>) event1 -> {
//                run3(model);
                run4(model);
            });

            dialog.open();
        } else {
//            run3(model);
            run4(model);
        }
    }


//    private void run3(GeneratorModel model) {
//        Generator generator = generatorService.get(model.getId()).get();
//        try {
//
//            UI ui = UI.getCurrent();
//            GeneratorRunner runner = runnerService.run(generator, ui);
//            addRunnerToUI(runner);
//            registerRunnerInContext(runner);
//
//        } catch (Exception e) {
//            ConfirmDialog dialog = ConfirmDialog.createError()
//                    .withCaption("The runner for Generator " + model.getNumber() + " returned an error")
//                    .withMessage(e.getMessage())
//                    .withCancelButton();
//            dialog.open();
//        }
//    }





    /**
     * update the current filter
     */
    private void filter() {
        Generator entity = new Generator();


        if (filtSymbol != null) {
            MarketIndex marketIndex = new MarketIndex();
            marketIndex.setSymbol(filtSymbol);
            entity.setIndex(marketIndex);
        }

        filter = Example.of(entity);

        refreshGrid();

    }

    private void refreshGrid() {
        grid.select(null);
        grid.getDataProvider().refreshAll();
    }


//    /**
//     * Add a GeneratorRunner to the UI and and attach the listeners
//     */
//    private void addRunnerToUI(GeneratorRunner runner) {
//
//        // add listener
//        runner.setRunnerListener(new GeneratorRunner.RunnerListener() {
//
//            UI ui = UI.getCurrent();
//
//            @Override
//            public void onAborted() {
//                removeRunnerFromUI(runner);
//                unregisterRunnerFromContext(runner);
//            }
//
//            @Override
//            public void onClosed() {
//                removeRunnerFromUI(runner);
//                unregisterRunnerFromContext(runner);
//            }
//
//        });
//
//        // add the runner to the status layout
//        statusLayout.add(runner);
//
//    }

//    /**
//     * Remove a GeneratorRunner from the UI panel
//     */
//    private void removeRunnerFromUI(GeneratorRunner runner) {
//        UI ui = UI.getCurrent();
//        ui.access((Command) () -> statusLayout.remove(runner));
//    }


//    /**
//     * Retrieve runners from Session Context
//     */
//    private List<GeneratorRunner> getContextRunners() {
//        UI ui = UI.getCurrent();
//        Object obj = ui.getSession().getAttribute(RUNNERS_KEY);
//        List<GeneratorRunner> runners;
//        if (obj == null) {
//            runners = new ArrayList<>();
//            ui.getSession().setAttribute(RUNNERS_KEY, runners);
//        } else {
//            runners = (ArrayList) obj;
//        }
//        return runners;
//    }


//    // add GeneratorRunner to Session context
//    private void registerRunnerInContext(GeneratorRunner runner) {
//        UI ui = UI.getCurrent();
//        Object obj = ui.getSession().getAttribute(RUNNERS_KEY);
//        List<GeneratorRunner> runners;
//        if (obj == null) {
//            runners = new ArrayList<>();
//            ui.getSession().setAttribute(RUNNERS_KEY, runners);
//        } else {
//            runners = (ArrayList) obj;
//        }
//        runners.add(runner);
//    }


//    // remove GeneratorRunner from Session context
//    private void unregisterRunnerFromContext(GeneratorRunner runner) {
//        UI ui = UI.getCurrent();
//        Object obj = ui.getSession().getAttribute(RUNNERS_KEY);
//        List<GeneratorRunner> runners;
//        if (obj != null) {
//            runners = (ArrayList) obj;
//            runners.remove(runner);
//        }
//
//    }


    private void run4(GeneratorModel model){

        try {

            SimulationCallable callable = runnerService.startGenerator(model);
            attachMonitorToTask(callable);

        } catch (Exception e) {
            ConfirmDialog dialog = ConfirmDialog.createError()
                    .withCaption("Error starting Generator id " + model.getId())
                    .withMessage(e.getMessage())
                    .withCancelButton();
            dialog.open();
        }


    }


    /**
     * Attach a TaskMonitor to a task and add it to the status panel
     */
    private void attachMonitorToTask(SimulationCallable callable){

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



}
