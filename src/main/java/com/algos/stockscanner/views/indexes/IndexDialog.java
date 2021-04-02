package com.algos.stockscanner.views.indexes;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;


/**
 * Dialog to edit an index
 */
@CssImport("./views/indexes/indexes-dialog.css")
public class IndexDialog extends Dialog {

    public IndexDialog(IndexModel model) {
        init();
    }

    private void init(){
        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);
        add(buildContent());
    }


    private Component buildContent(){

        VerticalLayout layout = new VerticalLayout();
        layout.addClassName("indexes-dialog");
        Component body = new Text("You have unsaved changes that will be discarded if you navigate away.");
        Component footer = buildFooter();
        layout.add(body,  footer);

        return layout;

    }


    private Component buildFooter(){

        Button confirmButton = new Button("Confirm", event -> {
            close();
        });
        Button cancelButton = new Button("Cancel", event -> {
            close();
        });

        Div btnLayout = new Div();
        btnLayout.addClassName("btn-layout");
        Div spc = new Div();
        spc.addClassName("spc-div");
        btnLayout.add(confirmButton, spc, cancelButton);

        return btnLayout;
    }

}
