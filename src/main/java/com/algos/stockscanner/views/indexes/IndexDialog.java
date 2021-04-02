package com.algos.stockscanner.views.indexes;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.server.StreamResource;

import java.io.ByteArrayInputStream;


/**
 * Dialog to edit an index
 */
@CssImport("./views/indexes/indexes-dialog.css")
public class IndexDialog extends Dialog {

    private IndexModel model;
    private IndexDialogConfirmListener confirmListener;

    private Image symbolImage;
    private TextField symbolFld;
    private TextField nameFld;
    private NumberField buySpreadFld;

    private NumberField ovnSellDayFld;
    private NumberField ovnSellWEFld;
    private NumberField ovnBuyDayFld;
    private NumberField ovnBuyWEFld;

    public IndexDialog(IndexModel model, IndexDialogConfirmListener confirmListener) {
        this.model=model;
        this.confirmListener=confirmListener;
        init();
    }

    private void init(){
        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);
        add(buildContent());
    }


    private Component buildContent(){

        Div layout = new Div();
        layout.addClassName("indexes-dialog");
        Component header =buildHeader();
        Component body =buildBody();
        Component footer = buildFooter();
        layout.add(header, body, footer);

        return layout;

    }


    private Component buildHeader(){
        Div header = new Div();
        header.addClassName("header");
        Icon icon = VaadinIcon.LINE_BAR_CHART.create();
        Label title = new Label("Index");
        header.add(icon, title);
        return header;
    }

    private Component buildBody(){

        Div body = new Div();
        body.addClassName("body");

//        Component imgComp= buildImage();

        //Component body = new Text("You have unsaved changes that will be discarded if you navigate away.");

        symbolFld = new TextField();
        symbolFld.setLabel("Symbol");

        nameFld = new TextField();
        nameFld.setLabel("Name");

        buySpreadFld = new NumberField();
        buySpreadFld.setLabel("Buy spread %");

        Div divSell = new Div();
        divSell.addClassName("overnight-box");
        ovnSellDayFld = new NumberField();
        ovnSellDayFld.setLabel("Sell, per day, $");
        ovnSellWEFld = new NumberField();
        ovnSellWEFld.setLabel("Sell, weekend, $");
        divSell.add(ovnSellDayFld, ovnSellWEFld);

        Div divBuy = new Div();
        divBuy.addClassName("overnight-box");
        ovnBuyDayFld = new NumberField();
        ovnBuyDayFld.setLabel("Buy, per day, $");
        ovnBuyWEFld = new NumberField();
        ovnBuyWEFld.setLabel("Buy, weekend, $");
        divBuy.add(ovnBuyDayFld, ovnBuyWEFld);

        body.add(symbolFld, nameFld, buySpreadFld, divSell, divBuy);
        return body;
    }

    private Component buildFooter(){

        Div btnLayout = new Div();
        btnLayout.addClassName("footer");

        Button confirmButton = new Button("Confirm", event -> {
            model = buildModelFromDialog();
            confirmListener.onConfirm(model);
            close();
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Cancel", event -> {
            close();
        });

        btnLayout.add(cancelButton, confirmButton );

        return btnLayout;
    }


    private Component buildImage(){
        symbolImage = new Image("images/logo.png", "Image");

        //Div imageDiv = new Div();
        //imageDiv.add();
        //byte[] imageBytes = new byte[];

        //StreamResource resource = new StreamResource("icons/icon.png", () -> new ByteArrayInputStream(imageBytes));
        //Image image = new Image(resource, "dummy image");
        //add(image);

        return symbolImage;
    }

    /**
     * Build a new model from the data displayed in the dialog
     */
    private IndexModel buildModelFromDialog(){
        IndexModel model = new IndexModel();
        //model.setImage(symbolImage.);
        model.setSymbol(symbolFld.getValue());
        model.setName(nameFld.getValue());
        model.setBuySpreadPercent(getDouble(buySpreadFld));
        model.setOvnSellDay(getDouble(ovnSellDayFld));
        model.setOvnSellWe(getDouble(ovnSellWEFld));
        model.setOvnBuyDay(getDouble(ovnBuyDayFld));
        model.setOvnBuyWe(getDouble(ovnBuyWEFld));
        return model;
    }

    private Double getDouble(NumberField field){
        Double d = field.getValue();
        if(d!=null){
            return d;
        }
        return 0d;
    }
}
