package com.algos.stockscanner.views.simulations;

import java.util.Optional;

import com.algos.stockscanner.data.entity.Simulation;
import com.algos.stockscanner.data.service.SimulationService;
import com.algos.stockscanner.beans.Utils;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.artur.helpers.CrudServiceDataProvider;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.PageTitle;
import com.algos.stockscanner.views.main.MainView;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.data.converter.StringToIntegerConverter;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.datepicker.DatePicker;

//@Route(value = "simulations", layout = MainView.class)
@PageTitle("Simulations")
@CssImport("./views/simulations/simulations-view.css")
public class SimulationsViewOld extends Div {

    private Grid<Simulation> grid = new Grid<>(Simulation.class, false);

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

    private @Autowired Utils utils;

    public SimulationsViewOld(@Autowired SimulationService simulationService) {
        addClassName("simulations-view");
        // Create UI
        SplitLayout splitLayout = new SplitLayout();
        splitLayout.setSizeFull();

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);

        add(splitLayout);

        // Configure Grid
        //grid.addColumn("indexCode").setAutoWidth(true);
        grid.addColumn("startTs").setAutoWidth(true);
        grid.addColumn("endTs").setAutoWidth(true);
        grid.addColumn("amount").setAutoWidth(true);
        grid.addColumn("leverage").setAutoWidth(true);
        grid.addColumn("width").setAutoWidth(true);
        grid.addColumn("balancing").setAutoWidth(true);
        grid.addColumn("numBuy").setAutoWidth(true);
        grid.addColumn("numSell").setAutoWidth(true);
        grid.addColumn("plPercent").setAutoWidth(true);
        grid.setDataProvider(new CrudServiceDataProvider<>(simulationService));
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.setHeightFull();

        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                Optional<Simulation> simulationFromBackend = simulationService.get(event.getValue().getId());
                // when a row is selected but the data is no longer available, refresh grid
                if (simulationFromBackend.isPresent()) {
                    populateForm(simulationFromBackend.get());
                } else {
                    refreshGrid();
                }
            } else {
                clearForm();
            }
        });

        // Configure Form
        binder = new BeanValidationBinder<>(Simulation.class);

        // Bind fields. This where you'd define e.g. validation rules
        binder.forField(amount).withConverter(new StringToIntegerConverter("Only numbers are allowed")).bind("amount");
        binder.forField(leverage).withConverter(new StringToIntegerConverter("Only numbers are allowed"))
                .bind("leverage");
        binder.forField(width).withConverter(new StringToIntegerConverter("Only numbers are allowed")).bind("width");
        binder.forField(balancing).withConverter(new StringToIntegerConverter("Only numbers are allowed"))
                .bind("balancing");
        binder.forField(numBuy).withConverter(new StringToIntegerConverter("Only numbers are allowed"))
                .bind("num_buy");
        binder.forField(numSell).withConverter(new StringToIntegerConverter("Only numbers are allowed"))
                .bind("num_sell");
        binder.forField(plPercent).withConverter(new StringToIntegerConverter("Only numbers are allowed"))
                .bind("pl_percent");

        binder.bindInstanceFields(this);

        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (this.simulation == null) {
                    this.simulation = new Simulation();
                }
                binder.writeBean(this.simulation);

                simulationService.update(this.simulation);
                clearForm();
                refreshGrid();
                Notification.show("Simulation details stored.");
            } catch (ValidationException validationException) {
                Notification.show("An exception happened while trying to store the simulation details.");
            }
        });


        // customize the header
        addAttachListener((ComponentEventListener<AttachEvent>) attachEvent -> {
            Optional<Component> parent = getParent();
            if(parent.isPresent()){
                Optional<HorizontalLayout> customArea = utils.findCustomArea(parent.get());
                if(customArea.isPresent()){
                    customArea.get().removeAll();
                    customizeHeader(customArea.get());
                }
            }
        });

    }


    private void customizeHeader(HorizontalLayout header){
        header.add(new Label("Custom content"));
    }


    private void createEditorLayout(SplitLayout splitLayout) {
        Div editorLayoutDiv = new Div();
        editorLayoutDiv.setId("editor-layout");

        Div editorDiv = new Div();
        editorDiv.setId("editor");
        editorLayoutDiv.add(editorDiv);

        FormLayout formLayout = new FormLayout();
        //indexCode = new TextField("IndexCode");
        startTs = new DatePicker("Start_ts");
        endTs = new DatePicker("End_ts");
        amount = new TextField("Amount");
        leverage = new TextField("Leverage");
        width = new TextField("Width");
        balancing = new TextField("Balancing");
        numBuy = new TextField("Num_buy");
        numSell = new TextField("Num_sell");
        plPercent = new TextField("Pl_percent");
        Component[] fields = new Component[]{startTs, endTs, amount, leverage, width, balancing, numBuy,
                numSell, plPercent};

        for (Component field : fields) {
            ((HasStyle) field).addClassName("full-width");
        }
        formLayout.add(fields);
        editorDiv.add(formLayout);
        createButtonLayout(editorLayoutDiv);

        splitLayout.addToSecondary(editorLayoutDiv);
    }

    private void createButtonLayout(Div editorLayoutDiv) {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setId("button-layout");
        buttonLayout.setWidthFull();
        buttonLayout.setSpacing(true);
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonLayout.add(save, cancel);
        editorLayoutDiv.add(buttonLayout);
    }

    private void createGridLayout(SplitLayout splitLayout) {
        Div wrapper = new Div();
        wrapper.setId("grid-wrapper");
        wrapper.setWidthFull();
        splitLayout.addToPrimary(wrapper);
        wrapper.add(grid);
    }

    private void refreshGrid() {
        grid.select(null);
        grid.getDataProvider().refreshAll();
    }

    private void clearForm() {
        populateForm(null);
    }

    private void populateForm(Simulation value) {
        this.simulation = value;
        binder.readBean(this.simulation);

    }
}
