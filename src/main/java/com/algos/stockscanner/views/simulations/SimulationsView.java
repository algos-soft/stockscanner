package com.algos.stockscanner.views.simulations;

import com.algos.stockscanner.beans.Utils;
import com.algos.stockscanner.data.entity.Generator;
import com.algos.stockscanner.data.entity.MarketIndex;
import com.algos.stockscanner.data.entity.Simulation;
import com.algos.stockscanner.data.service.SimulationService;
import com.algos.stockscanner.views.main.MainView;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.renderer.LocalDateRenderer;
import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.StreamResource;
import org.claspina.confirmdialog.ConfirmDialog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Example;

import javax.annotation.PostConstruct;
import java.io.*;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Route(value = "simulations", layout = MainView.class)
@PageTitle("Simulations")
@CssImport(value = "./views/simulations/simulations-view.css")
@CssImport(value = "./views/simulations/simulations-grid.css", themeFor = "vaadin-grid")
public class SimulationsView extends Div implements HasUrlParameter<String>, AfterNavigationObserver {

    // table and export file headers
    public static final String H_DETAILS="details";
    public static final String H_NUMGEN="#gen";
    public static final String H_SYMBOL="sym";
    public static final String H_START="start";
    public static final String H_END="end";
    public static final String H_INITIAL_AMT="initial amt";
    public static final String H_AMPLITUDE="amp";
    public static final String H_DAYS_BACK="days back";
    public static final String H_TERMINATION_REASON="term";
    public static final String H_PL="P/L";
    public static final String H_PL_PERCENT="P/L%";
    public static final String H_SPREAD="spread";
    public static final String H_COMMISSION="commission";
    public static final String H_POINTS_SCANNED="pts scanned";
    public static final String H_NUM_POSITIONS_OPENED="# pos opened";
    public static final String H_POINTS_IN_OPEN="pts in open";
    public static final String H_POINTS_IN_CLOSE ="pts in close";
    public static final String H_MIN_SERIES_OPEN="min series open";
    public static final String H_MAX_SERIES_OPEN="max series open";

    private Grid<SimulationModel> grid;

    @Autowired
    private Utils utils;

    @Autowired
    private SimulationService simulationService;

    @Autowired
    ApplicationContext context;

    private Integer filtNumGen;
    private String filtSymbol;
    private Example<Simulation> filter; // current filter
    private List<QuerySortOrder> order; // current order
    private Integer generatorNumParam;

    private IntegerField generatorNumberFld;

    private Button anchorButton;

    private InputStream excelInputStream;

    public SimulationsView() {
    }

    @PostConstruct
    private void init() {

        addClassName("simulations-view");

        filter=Example.of(new Simulation());    // empty initial filter

        createGrid();

        Component filterPanel = createFilterPanel();

        VerticalLayout layout = new VerticalLayout();
        layout.getStyle().set("height","100%");
        layout.add(filterPanel, grid);

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
        Anchor download = new Anchor(new StreamResource("simulations.xls", () -> excelInputStream), "");
        download.getElement().setAttribute("download", true);
        download.add(anchorButton);
        add(download);

    }


    @Override
    public void setParameter(BeforeEvent event,  @OptionalParameter String parameter) {
        if(parameter!=null){
            generatorNumParam =Integer.parseInt(parameter);
        }
    }

    @Override
    public void afterNavigation(  AfterNavigationEvent event) {
        // filter by generator passed as param
        if(generatorNumParam!=null){
            generatorNumberFld.setValue(generatorNumParam);
        }
    }



    private void customizeHeader(HorizontalLayout header) {

        header.getStyle().set("flex-direction", "row-reverse");

        Button addButton = new Button("Export", new Icon(VaadinIcon.ARROW_CIRCLE_DOWN_O));
        addButton.getStyle().set("margin-left", "1em");
        addButton.getStyle().set("margin-right", "1em");
        addButton.setIconAfterText(true);
        addButton.addClickListener((ComponentEventListener<ClickEvent<Button>>) buttonClickEvent -> {
            byte[] barray = simulationService.exportExcel(filter, order);
            if(barray!=null){
                excelInputStream = new ByteArrayInputStream(barray);
                anchorButton.clickInClient();
            }
        });

        header.add(addButton);
    }



    private Component createFilterPanel(){

        generatorNumberFld = new IntegerField("#gen ");
        generatorNumberFld.addValueChangeListener((HasValue.ValueChangeListener<AbstractField.ComponentValueChangeEvent<IntegerField, Integer>>) event -> {
            filtNumGen=event.getValue();
            filter();
        });


        ComboBox<MarketIndex> indexCombo = utils.buildIndexCombo();
        indexCombo.addValueChangeListener(new HasValue.ValueChangeListener<AbstractField.ComponentValueChangeEvent<ComboBox<MarketIndex>, MarketIndex>>() {
            @Override
            public void valueChanged(AbstractField.ComponentValueChangeEvent<ComboBox<MarketIndex>, MarketIndex> event) {
                filtSymbol=null;
                MarketIndex marketIndex = event.getValue();
                if(marketIndex!=null){
                    filtSymbol = marketIndex.getSymbol();
                }
                filter();
            }
        });

        HorizontalLayout layout = new HorizontalLayout();
        layout.add(generatorNumberFld, indexCombo);
        return layout;
    }


    private void createGrid() {

        CallbackDataProvider<SimulationModel, Void> provider;
        provider = DataProvider.fromCallbacks(fetchCallback -> {
            int offset = fetchCallback.getOffset();
            int limit = fetchCallback.getLimit();
            order = fetchCallback.getSortOrders();
            return simulationService.fetch(offset, limit, filter, order).stream();
        }, countCallback -> {
            return simulationService.count(filter);
        });

        grid = new Grid();

        grid.setDataProvider(provider);
        grid.setColumnReorderingAllowed(true);


        Grid.Column<SimulationModel> col;

        // data button
        col = grid.addComponentColumn(item -> createDataButton(grid, item));
        col.setHeader(H_DETAILS);

        // generator number
        col=grid.addColumn(SimulationModel::getNumGenerator);
        col.setHeader(H_NUMGEN);
        col.setSortProperty("generator.number");
        col.setWidth("5em");
        col.setResizable(true);

        // symbol
        col = grid.addComponentColumn(item -> createSymbolComponent(grid, item));
        col.setHeader(H_SYMBOL);
        col.setSortProperty("index.symbol");
        col.setResizable(true);

        // start date
        col=grid.addColumn(new LocalDateRenderer<>(SimulationModel::getStartTs,DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)));
        col.setHeader(H_START);
        col.setSortProperty("startTs");
        col.setWidth("6em");
        col.setResizable(true);

        // end date
        col=grid.addColumn(new LocalDateRenderer<>(SimulationModel::getEndTs,DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)));
        col.setHeader(H_END);
        col.setSortProperty("endTs");
        col.setWidth("6em");
        col.setResizable(true);

        // initial amount
        col=grid.addColumn(new NumberRenderer<>(SimulationModel::getInitialAmount, "%,.2f",Locale.getDefault()));
        col.setHeader(H_INITIAL_AMT);
        col.setSortProperty("initialAmount");
        col.setResizable(true);

        // amplitude
        col=grid.addColumn(new NumberRenderer<>(SimulationModel::getAmplitude, "%,.0f",Locale.getDefault()));
        col.setHeader(H_AMPLITUDE);
        col.setWidth("5em");
        col.setSortProperty("amplitude");
        col.setResizable(true);

        // days lookback
        col=grid.addColumn(SimulationModel::getDaysLookback);
        col.setHeader(H_DAYS_BACK);
        col.setWidth("5em");
        col.setSortProperty("daysLookback");
        col.setResizable(true);

        // termination
        col=grid.addColumn(SimulationModel::getTerminationCode);
        col.setHeader(H_TERMINATION_REASON);
        col.setWidth("11em");
        col.setSortProperty("terminationCode");
        col.setResizable(true);

        // P/L
        col=grid.addColumn(new NumberRenderer<>(SimulationModel::getPl, "%,.2f",Locale.getDefault()));
        col.setHeader(H_PL);
        col.setWidth("5em");
        col.setSortProperty("pl");
        col.setResizable(true);
        col.setClassNameGenerator(item -> getStyle(item.getPl()));

        // P/L percent
        col=grid.addColumn(new NumberRenderer<>(SimulationModel::getPlPercent, "%,.2f%%",Locale.getDefault()));
        col.setHeader(H_PL_PERCENT);
        col.setWidth("5em");
        col.setSortProperty("plPercent");
        col.setResizable(true);
        col.setClassNameGenerator(item -> getStyle(item.getPlPercent()));

        // tot spread
        col=grid.addColumn(new NumberRenderer<>(SimulationModel::getTotSpread, "%,.2f",Locale.getDefault()));
        col.setHeader(H_SPREAD);
        col.setWidth("5em");
        col.setSortProperty("totSpread");
        col.setResizable(true);

        // tot commission
        col=grid.addColumn(new NumberRenderer<>(SimulationModel::getTotCommission, "%,.2f",Locale.getDefault()));
        col.setHeader(H_COMMISSION);
        col.setWidth("5em");
        col.setSortProperty("totCommission");
        col.setResizable(true);

        // num points scanned
        col=grid.addColumn(new NumberRenderer<>(SimulationModel::getNumPointsScanned, "%,d",Locale.getDefault()));
        col.setHeader(H_POINTS_SCANNED);
        col.setWidth("5em");
        col.setSortProperty("numPointsScanned");
        col.setResizable(true);

        // num openings
        col=grid.addColumn(new NumberRenderer<>(SimulationModel::getNumOpenings, "%,d",Locale.getDefault()));
        col.setHeader(H_NUM_POSITIONS_OPENED);
        col.setWidth("5em");
        col.setSortProperty("numOpenings");
        col.setResizable(true);

        // num points opened
        col=grid.addColumn(new NumberRenderer<>(SimulationModel::getNumPointsHold, "%,d",Locale.getDefault()));
        col.setHeader(H_POINTS_IN_OPEN);
        col.setWidth("5em");
        col.setSortProperty("numPointsHold");
        col.setResizable(true);

        // num points closed
        col=grid.addColumn(new NumberRenderer<>(SimulationModel::getNumPointsWait, "%,d",Locale.getDefault()));
        col.setHeader(H_POINTS_IN_CLOSE);
        col.setWidth("5em");
        col.setSortProperty("numPointsWait");
        col.setResizable(true);

        // min series open
        col=grid.addColumn(new NumberRenderer<>(SimulationModel::getMinPointsHold, "%,d",Locale.getDefault()));
        col.setHeader(H_MIN_SERIES_OPEN);
        col.setWidth("5em");
        col.setSortProperty("minPointsHold");
        col.setResizable(true);

        // max series open
        col=grid.addColumn(new NumberRenderer<>(SimulationModel::getMaxPointsHold, "%,d",Locale.getDefault()));
        col.setHeader(H_MAX_SERIES_OPEN);
        col.setWidth("5em");
        col.setSortProperty("maxPointsHold");
        col.setResizable(true);

    }


    /**
     * return black, red, while styles based on signum
     */
    private String getStyle(float number){
        String style="";
        if(number!=0){
            if(number>0){
                style="positive";
            }else{
                style="negative";
            }
        }
        return style;
    }



    private Component createDataButton(Grid grid, SimulationModel item){
        Button button = new Button("data", clickEvent -> {
            Simulation simulation = simulationService.get(item.getId()).get();
            Component comp = context.getBean(SimulationDataViewer.class, simulation);
            ConfirmDialog dialog = ConfirmDialog.create().withMessage(comp);
            dialog.setWidth("100%");
            dialog.setResizable(true);
            dialog.setDraggable(true);
            dialog.open();
        });
        button.addClassName("databutton");
        return button;
    }


    private Component createSymbolComponent(Grid grid, SimulationModel item){
        Image img = item.getImage();
        img.setWidth("1.4em");
        img.getStyle().set("border-radius","10%");
        Span name = new Span(item.getSymbol());
        name.getStyle().set("margin-left","0.4em");
        HorizontalLayout layout = new HorizontalLayout();
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        layout.add(img, name);
        return layout;
    }


    private void refreshGrid() {
        grid.select(null);
        grid.getDataProvider().refreshAll();
    }

    /**
     * update the current filter
     */
    private void filter(){
        Simulation sim = new Simulation();

        if(filtNumGen!=null){
            Generator gen = new Generator();
            gen.setNumber(filtNumGen);
            sim.setGenerator(gen);
        }

        if(filtSymbol!=null){
            MarketIndex marketIndex = new MarketIndex();
            marketIndex.setSymbol(filtSymbol);
            sim.setIndex(marketIndex);
        }

        filter = Example.of(sim);

        refreshGrid();

    }

}
