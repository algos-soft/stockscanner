package com.algos.stockscanner.views.generators;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * Dialog to display and control an IndexPicker
 */
@CssImport("./views/generators/indexpicker-dialog-old.css")
@org.springframework.stereotype.Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class IndexesPickerDialogOld extends Dialog  {

    private IndexPickerDialogConfirmListener confirmListener;

    @Autowired
    private ApplicationContext context;

    private IndexPickerOld indexPicker;

    private List<Integer> selectedIds;

    private Label selectionIndicator;


    public IndexesPickerDialogOld(IndexPickerDialogConfirmListener confirmListener, List<Integer> selectedIds) {
        this.confirmListener = confirmListener;
        this.selectedIds=selectedIds;
    }


    @PostConstruct
    private void init() {
        setWidth("50em");
        setHeight("40em");
        setCloseOnEsc(false);
        setCloseOnOutsideClick(true);
        setResizable(true);
        setDraggable(true);

        add(buildContent());
    }


    private Component buildContent() {
        VerticalLayout layout = new VerticalLayout();
        layout.addClassName("dialog");

        Component header = buildHeader();
        Component body = buildBody();
        Component footer = buildFooter();
        layout.add(header, body, footer);
        return layout;
    }

    private Component buildHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.addClassName("dialogheader");

        TextField filterFld= new TextField("Filter");
        filterFld.addClassName("filterfield");
        filterFld.setClearButtonVisible(true);

        filterFld.setValueChangeMode(ValueChangeMode.EAGER);
        filterFld.addValueChangeListener((HasValue.ValueChangeListener<AbstractField.ComponentValueChangeEvent<TextField, String>>) event -> {
            String value = event.getValue();
            indexPicker.filter(value);
        });

        Button buttonAll = new Button("Select all");
        buttonAll.addClassName("selectbutton");
        buttonAll.addClickListener(new ComponentEventListener<ClickEvent<Button>>() {
            @Override
            public void onComponentEvent(ClickEvent<Button> buttonClickEvent) {
                indexPicker.selectAll();
            }
        });

        Button buttonNone = new Button("Select none");
        buttonNone.addClassName("selectbutton");
        buttonNone.addClickListener(new ComponentEventListener<ClickEvent<Button>>() {
            @Override
            public void onComponentEvent(ClickEvent<Button> buttonClickEvent) {
                indexPicker.selectNone();
            }
        });

        selectionIndicator = new Label();
        selectionIndicator.addClassName("selectionindicator");

        header.add(filterFld, buttonAll, buttonNone, selectionIndicator);
        return header;
    }



    private Component buildBody() {
        Div body = new Div();
        body.addClassName("dialogbody");

        IndexPickerSelectionListener selectionListener = (item, selected, totSelected) -> {
            selectionIndicator.setText("selected: "+ totSelected);
        };

        indexPicker=context.getBean(IndexPickerOld.class, selectedIds, selectionListener);
        body.add(indexPicker);
        return body;
    }

    private Component buildFooter() {
        HorizontalLayout footer = new HorizontalLayout();
        footer.addClassName("dialogfooter");

        Button confirmButton = new Button("Confirm", event -> {
            List<Integer> selectedIds=indexPicker.getSelectedIds();
            confirmListener.onConfirm(selectedIds);
            close();
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Cancel", event -> {
            close();
        });

        footer.add(cancelButton, confirmButton);

        return footer;
    }


//    private void populateBody(){
//        List<MarketIndex> entities = marketIndexService.findAllOrderBySymbol();
//        for(MarketIndex entity : entities){
//            IndexModel model = new IndexModel();
//            marketIndexService.entityToModel(entity, model);
//            IndexComponent comp = context.getBean(IndexComponent.class, utils.toPrimitive(model.getId()), model.getImage(), model.getSymbol());
//
//        }
//    }




}
