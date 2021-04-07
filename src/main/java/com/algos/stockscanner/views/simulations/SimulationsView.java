package com.algos.stockscanner.views.simulations;

import com.algos.stockscanner.beans.Utils;
import com.algos.stockscanner.data.entity.Simulation;
import com.algos.stockscanner.data.service.SimulationService;
import com.algos.stockscanner.views.main.MainView;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;

@Route(value = "simulations", layout = MainView.class)
@PageTitle("Simulations")
@CssImport("./views/simulations/simulations-view.css")
public class SimulationsView extends Div {

    private Grid<SimulationModel> grid;

    private TextField indexCode;
    private DatePicker startTs;
    private DatePicker endTs;
    private TextField amount;
    private TextField leverage;
    private TextField width;
    private TextField balancing;
    private TextField numBuy;
    private TextField numSell;
    private TextField plPercent;

    private Button cancel = new Button("Cancel");
    private Button save = new Button("Save");

    private BeanValidationBinder<Simulation> binder;

    private Simulation simulation;

    private @Autowired
    Utils utils;

    private @Autowired
    SimulationService simulationService;

    public SimulationsView() {
    }

    @PostConstruct
    private void init() {

        addClassName("simulations-view");
        createGrid();
        add(grid);


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


    private void createGrid() {

        CallbackDataProvider<SimulationModel, Void> provider;
        provider = DataProvider.fromCallbacks(fetchCallback -> {
            int offset = fetchCallback.getOffset();
            int limit = fetchCallback.getLimit();
            List<QuerySortOrder> sorts = fetchCallback.getSortOrders();
            return simulationService.fetch(offset, limit, sorts)
                    .stream();
        }, countCallback -> {
            return simulationService.count();
        });

        grid = new Grid<>();
        //grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        grid.setDataProvider(provider);

        grid.addColumn(SimulationModel::getId, "Id").setHeader("Id");
        grid.addColumn(SimulationModel::getSymbol).setHeader("Symbol");
        grid.addColumn(SimulationModel::getStartTs).setHeader("Start");
        grid.addColumn(SimulationModel::getEndTs).setHeader("End");

    }


    private void refreshGrid() {
        grid.select(null);
        grid.getDataProvider().refreshAll();
    }

}
