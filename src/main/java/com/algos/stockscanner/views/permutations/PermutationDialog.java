package com.algos.stockscanner.views.permutations;

import com.algos.stockscanner.beans.Utils;
import com.algos.stockscanner.data.enums.IndexCategories;
import com.algos.stockscanner.data.entity.MarketIndex;
import com.algos.stockscanner.data.service.MarketIndexService;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicReference;


/**
 * Dialog to edit a Permutation
 */
@CssImport("./views/permutations/permutations-dialog.css")
@org.springframework.stereotype.Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PermutationDialog extends Dialog {

    private static final int MAX_IMG_WIDTH = 64;
    private static final int MAX_IMG_HEIGHT = 64;

    private PermutationModel model;
    private PermutationDialogConfirmListener confirmListener;

    private ComboBox<MarketIndex> indexCombo;


    private byte[] imageData;
    private TextField symbolFld;
    private TextField nameFld;
    private Select<IndexCategories> categoryFld;
    private NumberField buySpreadFld;

    private NumberField ovnSellDayFld;
    private NumberField ovnSellWEFld;
    private NumberField ovnBuyDayFld;
    private NumberField ovnBuyWEFld;

    private Div imgPlaceholder;

    @Autowired
    private Utils utils;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private MarketIndexService marketIndexService;


    public PermutationDialog() {
        this((PermutationDialogConfirmListener) null);
    }

    public PermutationDialog(PermutationDialogConfirmListener confirmListener) {
        this((PermutationModel) null, confirmListener);
        this.model = model;
    }

    public PermutationDialog(PermutationModel model, PermutationDialogConfirmListener confirmListener) {
        this.model = model;
        this.confirmListener = confirmListener;
    }


    @PostConstruct
    private void init() {
        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);
        add(buildContent());

        if (model != null) {
            populateFromModel();
        }
    }


    private Component buildContent() {

        Div layout = new Div();
        layout.addClassName("indexes-dialog");
        Component header = buildHeader();
        Component body = buildBody();
        Component footer = buildFooter();
        layout.add(header, body, footer);

        return layout;

    }


    private Component buildHeader() {
        Div header = new Div();
        header.addClassName("header");

        // load default icon
        Resource res = context.getResource("images/rubik.jpg");
        byte[] imageData = null;
        try {
            imageData = Files.readAllBytes(Paths.get(res.getURI()));
            imageData = utils.scaleImage(imageData, MAX_IMG_WIDTH, MAX_IMG_HEIGHT);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Image img = utils.byteArrayToImage(imageData);
        img.setWidth(2, Unit.EM);
        img.setHeight(2, Unit.EM);

        Label title = new Label("Permutation");
        header.add(img, title);
        return header;
    }

    /**
     * Updates the icon in the header based on the current byte array
     */
    private void updateIcon() {
        Image img = utils.byteArrayToImage(imageData);
        imgPlaceholder.removeAll();
        img.setWidth(3, Unit.EM);
        img.setHeight(3, Unit.EM);
        imgPlaceholder.add(img);
    }


    private Component buildBody() {

        Div body = new Div();
        body.addClassName("body");

        buildCombo();

        body.add(indexCombo);

//        indexFld.setItems(IndexCategories.values());
//        indexFld.setTextRenderer(new ItemLabelGenerator<IndexCategories>() {
//            @Override
//            public String apply(IndexCategories item) {
//                return item.getDescription();
//            }
//        });
//
//
////        Component imgComp= buildImage();
//
//        //Component body = new Text("You have unsaved changes that will be discarded if you navigate away.");
//
//        symbolFld = new TextField();
//        symbolFld.setLabel("Symbol");
//
//        nameFld = new TextField();
//        nameFld.setLabel("Name");
//
//        categoryFld = new Select<IndexCategories>();
//        categoryFld.setLabel("Category");
//        categoryFld.setItems(IndexCategories.values());
//        categoryFld.setTextRenderer(new ItemLabelGenerator<IndexCategories>() {
//            @Override
//            public String apply(IndexCategories item) {
//                return item.getDescription();
//            }
//        });
//
//        buySpreadFld = new NumberField();
//        buySpreadFld.setLabel("Buy spread %");
//
//        Div divSell = new Div();
//        divSell.addClassName("overnight-box");
//        ovnSellDayFld = new NumberField();
//        ovnSellDayFld.setLabel("Sell, per day, $");
//        ovnSellWEFld = new NumberField();
//        ovnSellWEFld.setLabel("Sell, weekend, $");
//        divSell.add(ovnSellDayFld, ovnSellWEFld);
//
//        Div divBuy = new Div();
//        divBuy.addClassName("overnight-box");
//        ovnBuyDayFld = new NumberField();
//        ovnBuyDayFld.setLabel("Buy, per day, $");
//        ovnBuyWEFld = new NumberField();
//        ovnBuyWEFld.setLabel("Buy, weekend, $");
//        divBuy.add(ovnBuyDayFld, ovnBuyWEFld);
//
//        body.add(symbolFld, nameFld, categoryFld, buySpreadFld, divSell, divBuy);
        return body;
    }


    private void buildCombo() {

        // create a DataProvider with filtering callbacks
        MarketIndex exampleItem = new MarketIndex();
        ExampleMatcher matcher = ExampleMatcher.matchingAny().withMatcher("symbol", ExampleMatcher.GenericPropertyMatchers.startsWith().ignoreCase());
        Example<MarketIndex> example = Example.of(exampleItem, matcher);
        DataProvider<MarketIndex, String> dataProvider = DataProvider.fromFilteringCallbacks(fetchCallback -> {
            AtomicReference<String> filter=new AtomicReference<>();
            fetchCallback.getFilter().ifPresent( x -> filter.set(x));
            exampleItem.setSymbol(filter.get());
            return marketIndexService.fetch(fetchCallback.getOffset(), fetchCallback.getLimit(), example, null).stream();
        }, countCallback -> {
            AtomicReference<String> filter=new AtomicReference<>();
            countCallback.getFilter().ifPresent( x -> filter.set(x));
            exampleItem.setSymbol(filter.get());
            return marketIndexService.count(example);
        });

        // create a renderer for the items in the combo list
        Renderer<MarketIndex> listItemRenderer = new ComponentRenderer<>(item -> {
            Div text = new Div();
            text.setText(item.getSymbol());

            Image image = utils.byteArrayToImage(item.getImage());
            image.setWidth("2em");
            image.setHeight("2em");

            FlexLayout wrapper = new FlexLayout();
            text.getStyle().set("margin-left", "0.5em");
            wrapper.add(image, text);
            return wrapper;
        });

        indexCombo = new ComboBox<>();
        indexCombo.setLabel("Index");
        indexCombo.setDataProvider(dataProvider);
        indexCombo.setRenderer(listItemRenderer);
        indexCombo.setRequired(true);

    }


    private Component buildFooter() {

        Div btnLayout = new Div();
        btnLayout.addClassName("footer");

        Button confirmButton = new Button("Confirm", event -> {
            PermutationModel model = modelFromDialog();
            confirmListener.onConfirm(model);
            close();
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Cancel", event -> {
            close();
        });

        btnLayout.add(cancelButton, confirmButton);

        return btnLayout;
    }


    private Component buildImage() {
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
    private PermutationModel modelFromDialog() {
        PermutationModel model;
        if (this.model != null) {
            model = this.model;
        } else {
            model = new PermutationModel();
        }

//        model.setImageData(imageData);
//        model.setImage(utils.byteArrayToImage(imageData));
//        model.setSymbol(symbolFld.getValue());
//        model.setName(nameFld.getValue());
//        model.setCategory(categoryFld.getValue());
//        model.setBuySpreadPercent(getDouble(buySpreadFld));
//        model.setOvnSellDay(getDouble(ovnSellDayFld));
//        model.setOvnSellWe(getDouble(ovnSellWEFld));
//        model.setOvnBuyDay(getDouble(ovnBuyDayFld));
//        model.setOvnBuyWe(getDouble(ovnBuyWEFld));
        return model;
    }


    private void populateFromModel() {
//        imageData=model.getImageData();
//        updateIcon();
//        symbolFld.setValue(model.getSymbol());
//        nameFld.setValue(model.getName());
//        categoryFld.setValue(model.getCategory());
//        buySpreadFld.setValue(model.getBuySpreadPercent());
//        ovnSellDayFld.setValue(model.getOvnSellDay());
//        ovnSellWEFld.setValue(model.getOvnSellWe());
//        ovnBuyDayFld.setValue(model.getOvnBuyDay());
//        ovnBuyWEFld.setValue(model.getOvnBuyWe());
    }


    private Double getDouble(NumberField field) {
        Double d = field.getValue();
        if (d != null) {
            return d;
        }
        return 0d;
    }
}
