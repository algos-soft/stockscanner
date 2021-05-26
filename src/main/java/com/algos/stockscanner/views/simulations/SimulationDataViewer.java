package com.algos.stockscanner.views.simulations;

import com.algos.stockscanner.beans.Utils;
import com.algos.stockscanner.data.entity.Simulation;
import com.algos.stockscanner.data.entity.SimulationItem;
import com.algos.stockscanner.enums.Actions;
import com.algos.stockscanner.data.service.SimulationItemService;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import com.vaadin.flow.data.renderer.NumberRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Shows simulation data
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@CssImport(value = "./views/simulations/simulations-grid.css", themeFor = "vaadin-grid")
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


        Grid.Column<SimulationItemModel> col;

        // timestamp
        col=grid.addColumn(new LocalDateTimeRenderer<>(SimulationItemModel::getTimestamp, DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)));
        col.setHeader("timestamp");
        col.setWidth("5em");
        col.setResizable(true);

        // action
        col=grid.addColumn(SimulationItemModel::getAction);
        col.setHeader("action");
        col.setWidth("4em");
        col.setResizable(true);
        col.setClassNameGenerator(item -> getOpenCloseStyle(item.getAction()));

        // action type
        col=grid.addColumn(SimulationItemModel::getActionType);
        col.setHeader("type");
        col.setWidth("4em");
        col.setResizable(true);
        col.setClassNameGenerator(item -> getOpenCloseStyle(item.getAction()));

        // reason
        col=grid.addColumn(SimulationItemModel::getReason);
        col.setHeader("reason");
        col.setWidth("12em");
        col.setResizable(true);

        // ref price
        col=grid.addColumn(new NumberRenderer<>(SimulationItemModel::getRefPrice, "%,.2f",Locale.getDefault()));
        col.setHeader("ref price");
        col.setWidth("4em");
        col.setResizable(true);

        // curr price
        col=grid.addColumn(new NumberRenderer<>(SimulationItemModel::getCurrPrice, "%,.2f",Locale.getDefault()));
        col.setHeader("curr price");
        col.setWidth("4em");
        col.setResizable(true);

        // delta ampl
//        col=grid.addColumn(new NumberRenderer<>(SimulationItemModel::getDeltaAmpl, "%,.2f",Locale.getDefault()));
        col=grid.addColumn(new NumberRenderer<>(SimulationItemModel::getDeltaAmpl, "%+.2f",Locale.getDefault()));
        col.setHeader("delta%");
        col.setWidth("4em");
        col.setResizable(true);

        // amplitude dn
        col=grid.addColumn(new NumberRenderer<>(SimulationItemModel::getAmplitudeDn, "-%,.2f",Locale.getDefault()));
        col.setHeader("ampl dn%");
        col.setWidth("3em");
        col.setResizable(true);

        // amplitude up
        col=grid.addColumn(new NumberRenderer<>(SimulationItemModel::getAmplitudeUp, "+%,.2f",Locale.getDefault()));
        col.setHeader("ampl up%");
        col.setWidth("3em");
        col.setResizable(true);

        // spread amount
        col=grid.addColumn(new NumberRenderer<>(SimulationItemModel::getSpreadAmt, "%,.2f",Locale.getDefault()));
        col.setHeader("spread");
        col.setWidth("3em");
        col.setResizable(true);
        col.setClassNameGenerator(item -> getZeroHiddenStyle(item.getSpreadAmt()));

        // commission amount
        col=grid.addColumn(new NumberRenderer<>(SimulationItemModel::getCommissionAmt, "%,.2f",Locale.getDefault()));
        col.setHeader("commiss");
        col.setWidth("3em");
        col.setResizable(true);
        col.setClassNameGenerator(item -> getZeroHiddenStyle(item.getCommissionAmt()));

        // curr value
        col=grid.addColumn(new NumberRenderer<>(SimulationItemModel::getCurrValue, "%,.2f",Locale.getDefault()));
        col.setHeader("value");
        col.setWidth("4em");
        col.setResizable(true);
        col.setClassNameGenerator(item -> getZeroHiddenStyle(item.getCurrValue()));


        // P/L
        col=grid.addColumn(new NumberRenderer<>(SimulationItemModel::getPl, "%,.2f",Locale.getDefault()));
        col.setHeader("P/L");
        col.setWidth("4em");
        col.setResizable(true);
        col.setClassNameGenerator(item -> getPLStyle(item.getPl()));

    }


    /**
     * return black, red, white styles based on signum
     */
    private String getPLStyle(float number){
        String style="";
        if(number!=0){
            if(number>0){
                style="positive";
            }else{
                style="negative";
            }

        }else{
            style="zero";
        }
        return style;
    }

    /**
     * styles to hide zeroes
     */
    private String getZeroHiddenStyle(float number){
        String style="";
        if(number==0){
            style="zero";
        }
        return style;
    }





    /**
     * return strong style for OPEN and CLOSE actions
     */
    private String getOpenCloseStyle(Actions action){
        if(action.equals(Actions.OPEN) || action.equals(Actions.CLOSE)){
            return "openclose";
        }
        return "";
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
