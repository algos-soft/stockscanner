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
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.renderer.LocalDateRenderer;
import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;

import javax.annotation.PostConstruct;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Route(value = "simulations", layout = MainView.class)
@PageTitle("Simulations")
@CssImport(value = "./views/simulations/simulations-view.css", themeFor = "vaadin-grid")
public class SimulationsView extends Div {

    private Grid<SimulationModel> grid;

    @Autowired
    private Utils utils;

    @Autowired
    private SimulationService simulationService;

    private Integer filtNumGen;
    private String filtSymbol;
    private Example<Simulation> filter;

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

    }


    private void customizeHeader(HorizontalLayout header) {
        header.add(new Label("My custom content"));
    }


    private Component createFilterPanel(){

        IntegerField generatorNumberFld = new IntegerField("#gen ");
        generatorNumberFld.addValueChangeListener(new HasValue.ValueChangeListener<AbstractField.ComponentValueChangeEvent<IntegerField, Integer>>() {
            @Override
            public void valueChanged(AbstractField.ComponentValueChangeEvent<IntegerField, Integer> event) {
                filtNumGen=event.getValue();
                filter();
            }
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
            List<QuerySortOrder> sorts = fetchCallback.getSortOrders();
            return simulationService.fetch(offset, limit, filter, sorts).stream();
        }, countCallback -> {
            return simulationService.count(filter);
        });

        grid = new Grid();

        grid.setDataProvider(provider);
        grid.setColumnReorderingAllowed(true);


        Grid.Column col;

        // generator number
        col=grid.addColumn(SimulationModel::getNumGenerator);
        col.setHeader("#gen");
        col.setSortProperty("generator.number");

        // symbol
        col=grid.addColumn(SimulationModel::getSymbol);
        col.setHeader("symbol");
        col.setSortProperty("index.symbol");

        // data button
        col = grid.addComponentColumn(item -> createDataButton(grid, item));
        col.setHeader("data");
        col.setWidth("8em");

        // start date
        col=grid.addColumn(new LocalDateRenderer<>(SimulationModel::getStartTs,DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)));
        col.setHeader("start");
        col.setSortProperty("startTs");

        // end date
        col=grid.addColumn(new LocalDateRenderer<>(SimulationModel::getEndTs,DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)));
        col.setHeader("end");
        col.setSortProperty("endTs");

        // initial amount
        col=grid.addColumn(new NumberRenderer<>(SimulationModel::getInitialAmount, "%,.2f",Locale.getDefault()));
        col.setHeader("initial amt");
        col.setSortProperty("initialAmount");

        // leverage
        col=grid.addColumn(new NumberRenderer<>(SimulationModel::getLeverage, "%,d",Locale.getDefault()));
        col.setHeader("lev");
        col.setSortProperty("leverage");

        // amplitude
        col=grid.addColumn(new NumberRenderer<>(SimulationModel::getAmplitude, "%,.2f",Locale.getDefault()));
        col.setHeader("amp");
        col.setSortProperty("amplitude");

        // final amount
        col=grid.addColumn(new NumberRenderer<>(SimulationModel::getFinalAmount, "%,.2f",Locale.getDefault()));
        col.setHeader("final amt");
        col.setSortProperty("finalAmount");

        // P/L
        col=grid.addColumn(new NumberRenderer<>(SimulationModel::getPl, "%,.2f",Locale.getDefault()));
        col.setHeader("P/L");
        col.setSortProperty("pl");

        // P/L percent
        col=grid.addColumn(new NumberRenderer<>(SimulationModel::getPlPercent, "%,.2f%%",Locale.getDefault()));
        col.setHeader("P/L%");
        col.setSortProperty("plPercent");

        // num buy
        col=grid.addColumn(new NumberRenderer<>(SimulationModel::getNumBuy, "%,d",Locale.getDefault()));
        col.setHeader("# buy");
        col.setSortProperty("numBuy");

        // num buy
        col=grid.addColumn(new NumberRenderer<>(SimulationModel::getNumSell, "%,d",Locale.getDefault()));
        col.setHeader("# sell");
        col.setSortProperty("numSell");

        // tot spread
        col=grid.addColumn(new NumberRenderer<>(SimulationModel::getTotSpread, "%,.2f",Locale.getDefault()));
        col.setHeader("spread");
        col.setSortProperty("totSpread");

        // tot commission
        col=grid.addColumn(new NumberRenderer<>(SimulationModel::getTotCommission, "%,.2f",Locale.getDefault()));
        col.setHeader("commission");
        col.setSortProperty("totCommission");

        // num points scanned
        col=grid.addColumn(new NumberRenderer<>(SimulationModel::getNumPointsScanned, "%,d",Locale.getDefault()));
        col.setHeader("pts scanned");
        col.setSortProperty("numPointsScanned");

        // num points hold
        col=grid.addColumn(new NumberRenderer<>(SimulationModel::getNumPointsHold, "%,d",Locale.getDefault()));
        col.setHeader("pts hold");
        col.setSortProperty("numPointsHold");

        // num points wait
        col=grid.addColumn(new NumberRenderer<>(SimulationModel::getNumPointsWait, "%,d",Locale.getDefault()));
        col.setHeader("pts wait");
        col.setSortProperty("numPointsWait");

        // min points hold
        col=grid.addColumn(new NumberRenderer<>(SimulationModel::getMinPointsHold, "%,d",Locale.getDefault()));
        col.setHeader("min pts hold");
        col.setSortProperty("minPointsHold");

        // max points hold
        col=grid.addColumn(new NumberRenderer<>(SimulationModel::getMaxPointsHold, "%,d",Locale.getDefault()));
        col.setHeader("max pts hold");
        col.setSortProperty("maxPointsHold");


    }


    private Component createDataButton(Grid grid, SimulationModel item){
        Button button = new Button("data", clickEvent -> {
            Notification.show("id: "+item.getId()+" gen: "+item.getNumGenerator()+" "+item.getSymbol());
        });
        button.addClassName("databutton");
        return button;
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
