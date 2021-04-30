package com.algos.stockscanner.views.generators;

import com.algos.stockscanner.beans.Utils;
import com.algos.stockscanner.data.entity.MarketIndex;
import com.algos.stockscanner.data.service.MarketIndexService;
import com.algos.stockscanner.views.indexes.IndexModel;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * Dialog to display and control an IndexPicker
 */
@CssImport("./views/generators/indexpicker-dialog.css")
@org.springframework.stereotype.Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class IndexesPickerDialog extends Dialog  {

    private IndexPickerDialogConfirmListener confirmListener;

    @Autowired
    private ApplicationContext context;

    private IndexPicker indexPicker;

    private List<Integer> selectedIds;

    public IndexesPickerDialog(IndexPickerDialogConfirmListener confirmListener, List<Integer> selectedIds) {
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
        Div header = new Div();
        header.addClassName("header");
        header.setText("Header");
        return header;
    }



    private Component buildBody() {
        Div body = new Div();
        body.addClassName("body");
        indexPicker=context.getBean(IndexPicker.class, selectedIds);
        body.add(indexPicker);
        return body;
    }

    private Component buildFooter() {
        HorizontalLayout footer = new HorizontalLayout();
        footer.addClassName("footer");

        Button confirmButton = new Button("Confirm", event -> {
//            GeneratorModel model = modelFromDialog();
//            confirmListener.onConfirm(model);
            confirmListener.onConfirm();
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
