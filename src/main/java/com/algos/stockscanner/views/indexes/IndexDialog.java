package com.algos.stockscanner.views.indexes;

import com.algos.stockscanner.beans.Utils;
import com.algos.stockscanner.enums.IndexCategories;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import org.claspina.confirmdialog.ButtonOption;
import org.claspina.confirmdialog.ConfirmDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;


/**
 * Dialog to edit an index
 */
@CssImport("./views/indexes/indexes-dialog.css")
@org.springframework.stereotype.Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class IndexDialog extends Dialog {

    private static final Logger log = LoggerFactory.getLogger(IndexDialog.class);

    private IndexModel model;
    private IndexDialogConfirmListener confirmListener;

    private byte[] imageData;
    private TextField symbolFld;
    private TextField nameFld;
    private Select<IndexCategories> categoryFld;
    private BigDecimalField buySpreadFld;

    private NumberField ovnSellDayFld;
    private NumberField ovnSellWEFld;
    private NumberField ovnBuyDayFld;
    private NumberField ovnBuyWEFld;

    private FlexLayout imgPlaceholder;

    @Value("${default.buy.spread.percent:0.15}")
    private float defaultBuySpreadPercent;

    @Autowired
    private Utils utils;


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

        imgPlaceholder=new FlexLayout();
        imgPlaceholder.setId("indexdialog-imgplaceholder");

        add(buildContent());

        if(model==null){
            BigDecimal bd = new BigDecimal(defaultBuySpreadPercent);
            bd=bd.round(new MathContext(2, RoundingMode.FLOOR));
            buySpreadFld.setValue(bd);
        }else{
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
//        Div div = new Div();
//        div.addClassName("header");

        imgPlaceholder.addClickListener((ComponentEventListener<ClickEvent<FlexLayout>>) divClickEvent -> {
            changeIconByUrl();
        });

        // load default icon
        Image img = utils.getDefaultIndexIcon();
        byte[] bytes = utils.imageToByteArray(img);
        imageData=bytes;

        updateIcon();

        VerticalLayout description = new VerticalLayout();
        description.setSpacing(false);
        description.setPadding(false);
        Span span1=new Span();
        span1.setId("indexdialog-symbol");
        Span span2=new Span();
        span2.setId("indexdialog-name");
        if(model!=null){
            if(model.getSymbol()!=null){
                span1.setText(model.getSymbol());
            }
            if(model.getName()!=null){
                span2.setText(model.getName());
            }
        }else{
            span1.setText("New index");
        }
        description.add(span1, span2);

        HorizontalLayout header = new HorizontalLayout();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.add(imgPlaceholder, description);

        return header;
    }

    /**
     * Updates the icon in the header based on the current byte array
     */
    private void updateIcon(){
        Image img = utils.byteArrayToImage(imageData);
        imgPlaceholder.removeAll();
        img.setWidth(3, Unit.EM);
        img.setHeight(3, Unit.EM);
        imgPlaceholder.add(img);
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
                imageData = utils.getIconFromUrl(url);
                updateIcon();
            }catch (Exception e){
                log.error("could not retrieve icon from url "+url, e);
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

        categoryFld = new Select<IndexCategories>();
        categoryFld.setLabel("Category");
        categoryFld.setItems(IndexCategories.values());
        categoryFld.setTextRenderer(new ItemLabelGenerator<IndexCategories>() {
            @Override
            public String apply(IndexCategories item) {
                return item.getDescription();
            }
        });

        buySpreadFld = new BigDecimalField();
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

        body.add(symbolFld, nameFld, categoryFld, buySpreadFld, divSell, divBuy);
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
//        model.setImage(utils.byteArrayToImage(imageData));
        model.setSymbol(symbolFld.getValue());
        model.setName(nameFld.getValue());
        model.setCategory(categoryFld.getValue());
        model.setSpreadPercent(utils.toPrimitiveFloat(buySpreadFld.getValue()));
        model.setOvnSellDay(getFloat(ovnSellDayFld));
        model.setOvnSellWe(getFloat(ovnSellWEFld));
        model.setOvnBuyDay(getFloat(ovnBuyDayFld));
        model.setOvnBuyWe(getFloat(ovnBuyWEFld));
        return model;
    }


    private void populateFromModel(){
        imageData=model.getImageData();
        updateIcon();
        symbolFld.setValue(model.getSymbol());
        nameFld.setValue(model.getName());
        categoryFld.setValue(model.getCategory());
        buySpreadFld.setValue(getBigDecimal(model.getSpreadPercent()));
        ovnSellDayFld.setValue(new Double(model.getOvnSellDay()));
        ovnSellWEFld.setValue(new Double(model.getOvnSellWe()));
        ovnBuyDayFld.setValue(new Double(model.getOvnBuyDay()));
        ovnBuyWEFld.setValue(new Double(model.getOvnBuyWe()));
    }


    private Float getFloat(NumberField field){
        Double d=field.getValue();
        if(d!=null){
            return d.floatValue();
        }
        return 0f;
    }

    private BigDecimal getBigDecimal(float f){
        return new BigDecimal(f).round(new MathContext(2, RoundingMode.HALF_UP));
    }


}
