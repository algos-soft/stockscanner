package com.algos.stockscanner.views.indexes;

import com.algos.stockscanner.beans.HttpClient;
import com.algos.stockscanner.beans.Utils;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import okhttp3.Request;
import okhttp3.Response;
import org.claspina.confirmdialog.ButtonOption;
import org.claspina.confirmdialog.ConfirmDialog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.Resource;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


/**
 * Dialog to edit an index
 */
@CssImport("./views/indexes/indexes-dialog.css")
@org.springframework.stereotype.Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class IndexDialog extends Dialog {

    private static final int MAX_IMG_WIDTH=64;
    private static final int MAX_IMG_HEIGHT=64;

    private IndexModel model;
    private IndexDialogConfirmListener confirmListener;

    private byte[] imageData;
    private TextField symbolFld;
    private TextField nameFld;
    private NumberField buySpreadFld;

    private NumberField ovnSellDayFld;
    private NumberField ovnSellWEFld;
    private NumberField ovnBuyDayFld;
    private NumberField ovnBuyWEFld;

    private Div imgPlaceholder;

    @Autowired
    private Utils utils;

    @Autowired
    private HttpClient httpClient;

    @Autowired
    private ApplicationContext context;


    public IndexDialog() {
        this((IndexDialogConfirmListener)null);
    }

    public IndexDialog(IndexDialogConfirmListener confirmListener) {
        this((IndexModel)null, confirmListener);
        this.model=model;
    }

    public IndexDialog(IndexModel model, IndexDialogConfirmListener confirmListener) {
        this.model=model;
        this.confirmListener=confirmListener;
    }


    @PostConstruct
    private void init(){
        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);
        add(buildContent());

        if(model!=null){
            populateFromModel();
        }
    }


    private Component buildContent(){

        Div layout = new Div();
        layout.addClassName("indexes-dialog");
        Component header =buildHeader();
        Component body = buildBody();
        Component footer = buildFooter();
        layout.add(header, body, footer);

        return layout;

    }


    private Component buildHeader(){
        Div header = new Div();
        header.addClassName("header");

        imgPlaceholder=new Div();
        imgPlaceholder.addClickListener((ComponentEventListener<ClickEvent<Div>>) divClickEvent -> {
            changeIconByUrl();
        });

        // load default icon
        Resource res=context.getResource("images/generic_index.jpg");
        try {
            imageData = Files.readAllBytes(Paths.get(res.getURI()));
            imageData = utils.scaleImage(imageData, MAX_IMG_WIDTH, MAX_IMG_HEIGHT);
            updateIcon();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Label title = new Label("Index");
        header.add(imgPlaceholder, title);
        return header;
    }

    /**
     * Updates the icon in the header based on the current byte array
     */
    private void updateIcon(){
        Image img = utils.byteArrayToImage(imageData);
        imgPlaceholder.removeAll();
        img.setWidth(2, Unit.EM);
        img.setHeight(2, Unit.EM);
        imgPlaceholder.add(img);
    }


    private byte[] getBytes(String url) throws IOException {
        Request request = new Request.Builder().url(url).build();

        try (Response response = httpClient.newCall(request).execute()) {
            return response.body().bytes();
        }

    }

    /**
     * Ask for an URL in a dialog and change the index icon
     */
    private void changeIconByUrl(){

        TextField tf = new TextField();
        tf.setLabel("Icon URL");

        Button bConfirm = new Button();
        ConfirmDialog dialog = ConfirmDialog.create().withMessage(tf)
                .withButton(new Button(), ButtonOption.caption("Cancel"), ButtonOption.closeOnClick(true))
                .withButton(bConfirm, ButtonOption.caption("Confirm"), ButtonOption.focus(), ButtonOption.closeOnClick(true));

        bConfirm.addClickListener((ComponentEventListener<ClickEvent<Button>>) event1 -> {
            dialog.close();
            String url = tf.getValue();
            try {
                imageData = getBytes(url);
                imageData = utils.scaleImage(imageData, MAX_IMG_WIDTH, MAX_IMG_HEIGHT);
                updateIcon();
            }catch (Exception e){
                e.printStackTrace();
            }
        });

        dialog.setWidth("30em");
        dialog.open();
        tf.focus();

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
            IndexModel model = modelFromDialog();
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
        //symbolImage = new Image("images/logo.png", "Image");

        //Div imageDiv = new Div();
        //imageDiv.add();
        //byte[] imageBytes = new byte[];

        //StreamResource resource = new StreamResource("icons/icon.png", () -> new ByteArrayInputStream(imageBytes));
        //Image image = new Image(resource, "dummy image");
        //add(image);

//        Image image = new Image("https://dummyimage.com/600x400/000/fff", "DummyImage");
//        add(image);


        //return symbolImage;
        return null;
    }

    /**
     * Build a new model or update the current model from the data displayed in the dialog
     */
    private IndexModel modelFromDialog(){
        IndexModel model;
        if(this.model!=null){
            model=this.model;
        }else{
            model = new IndexModel();
        }

        model.setImageData(imageData);
        model.setImage(utils.byteArrayToImage(imageData));
        model.setSymbol(symbolFld.getValue());
        model.setName(nameFld.getValue());
        model.setBuySpreadPercent(getDouble(buySpreadFld));
        model.setOvnSellDay(getDouble(ovnSellDayFld));
        model.setOvnSellWe(getDouble(ovnSellWEFld));
        model.setOvnBuyDay(getDouble(ovnBuyDayFld));
        model.setOvnBuyWe(getDouble(ovnBuyWEFld));
        return model;
    }


    private void populateFromModel(){
        imageData=model.getImageData();
        updateIcon();
        symbolFld.setValue(model.getSymbol());
        nameFld.setValue(model.getName());
        buySpreadFld.setValue(model.getBuySpreadPercent());
        ovnSellDayFld.setValue(model.getOvnSellDay());
        ovnSellWEFld.setValue(model.getOvnSellWe());
        ovnBuyDayFld.setValue(model.getOvnBuyDay());
        ovnBuyWEFld.setValue(model.getOvnBuyWe());
    }


    private Double getDouble(NumberField field){
        Double d = field.getValue();
        if(d!=null){
            return d;
        }
        return 0d;
    }
}
