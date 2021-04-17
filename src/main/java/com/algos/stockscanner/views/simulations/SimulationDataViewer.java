package com.algos.stockscanner.views.simulations;

import com.algos.stockscanner.beans.Utils;
import com.algos.stockscanner.data.entity.MarketIndex;
import com.algos.stockscanner.data.entity.Simulation;
import com.algos.stockscanner.data.entity.SimulationItem;
import com.algos.stockscanner.data.enums.Actions;
import com.algos.stockscanner.data.service.SimulationItemService;
import com.algos.stockscanner.views.indexes.IndexModel;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.renderer.LocalDateRenderer;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;

/**
 * Shows simulation data
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SimulationDataViewer extends VerticalLayout {
    private Simulation simulation;

    private Grid<SimulationItemModel> grid;

    @Autowired
    private Utils utils;

    @Autowired
    private SimulationItemService simulationItemService;


    private Example<SimulationItem> filter;

    public SimulationDataViewer(Simulation simulation) {
        this.simulation = simulation;
    }

    @PostConstruct
    private void init(){

        SimulationItem probe = new SimulationItem();
        probe.setSimulation(simulation);
        filter=Example.of(probe);

        createGrid();

        Checkbox checkbox = new Checkbox("show STAY");
        checkbox.addValueChangeListener(new HasValue.ValueChangeListener<AbstractField.ComponentValueChangeEvent<Checkbox, Boolean>>() {
            @Override
            public void valueChanged(AbstractField.ComponentValueChangeEvent<Checkbox, Boolean> event) {
                updateFilter(event.getValue());
            }
        });

        add(grid, checkbox);

        loadAll(false);

    }


    private void updateFilter(boolean showStay){
        loadAll(showStay);
    }


    private void createGrid() {

        CallbackDataProvider<SimulationItemModel, Void> provider;
        provider = DataProvider.fromCallbacks(fetchCallback -> {
            int offset = fetchCallback.getOffset();
            int limit = fetchCallback.getLimit();
            List<QuerySortOrder> sorts = fetchCallback.getSortOrders();
            return simulationItemService.fetch(offset, limit, filter, sorts).stream();
        }, countCallback -> {
            return simulationItemService.count(filter);
        });

        grid = new Grid();

        //grid.setDataProvider(provider);
        grid.setColumnReorderingAllowed(true);


        Grid.Column col;

        // timestamp
        col=grid.addColumn(new LocalDateTimeRenderer<>(SimulationItemModel::getTimestamp, DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)));
        col.setHeader("timestamp");
        //col.setWidth("3em");
        col.setResizable(true);

        // action
        col=grid.addColumn(SimulationItemModel::getAction);
        col.setHeader("action");
        //col.setWidth("3em");
        col.setResizable(true);

        // action
        col=grid.addColumn(SimulationItemModel::getActionType);
        col.setHeader("type");
        //col.setWidth("7em");
        col.setResizable(true);

        // reason
        col=grid.addColumn(SimulationItemModel::getReason);
        col.setHeader("reason");
        //col.setWidth("7em");
        //col.setAutoWidth(true);
        col.setResizable(true);

        // ref price
        col=grid.addColumn(SimulationItemModel::getRefPrice);
        col.setHeader("ref price");
        //col.setWidth("7em");
        col.setResizable(true);

        // curr price
        col=grid.addColumn(SimulationItemModel::getCurrPrice);
        col.setHeader("curr price");
        //col.setWidth("7em");
        col.setResizable(true);

        // delta ampl
        col=grid.addColumn(SimulationItemModel::getDeltaAmpl);
        col.setHeader("delta%");
        //col.setWidth("7em");
        col.setResizable(true);

    }


    /**
     * Load data in the grid
     */
    private void loadAll(boolean showStay){
        List<SimulationItemModel> outList= simulationItemService.findBySimulationOrderByTimestamp(simulation);
        if(!showStay){
            outList=filterStay(outList);
        }
        grid.setItems(outList);
    }

    private List<SimulationItemModel> filterStay(List<SimulationItemModel> items){
        List<SimulationItemModel> outList=new ArrayList<>();
        for(SimulationItemModel item : items){
            if(!item.getAction().equals(Actions.STAY)){
                outList.add(item);
            }
        }
        return outList;
    }



}
