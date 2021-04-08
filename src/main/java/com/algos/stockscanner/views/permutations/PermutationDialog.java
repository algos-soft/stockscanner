package com.algos.stockscanner.views.permutations;

import com.algos.stockscanner.Application;
import com.algos.stockscanner.beans.Utils;
import com.algos.stockscanner.data.entity.MarketIndex;
import com.algos.stockscanner.data.service.MarketIndexService;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;


/**
 * Dialog to edit a Permutation
 */
@CssImport("./views/permutations/permutations-dialog.css")
@org.springframework.stereotype.Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PermutationDialog extends Dialog {

    private static final int MAX_IMG_WIDTH = 128;
    private static final int MAX_IMG_HEIGHT = 128;

    private PermutationModel model;
    private PermutationDialogConfirmListener confirmListener;

    private FlexLayout imgPlaceholder;

    private ComboBox<MarketIndex> indexCombo;
    private DatePicker startDatePicker;

    private NumberField amountFld;
    private IntegerField leverageFld;
    private NumberField stopLossFld;
    private NumberField takeProfitFld;

    private RadioButtonGroup<String> lengthRadioGroup;
    private IntegerField numberOfDays;

    private Checkbox permutateAmplitudeCheckbox;
    private IntegerField amplitudeFld;
    private IntegerField amplitudeMinFld;
    private IntegerField amplitudeMaxFld;
    private IntegerField amplitudeStepsFld;

    private Checkbox permutateAvgDaysCheckbox;
    private IntegerField avgDaysFld;
    private IntegerField avgDaysMinFld;
    private IntegerField avgDaysMaxFld;
    private IntegerField avgDaysStepsFld;

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
        setWidth("30em");
        setHeight("35em");
        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);
        add(buildContent());

        if (model != null) {
            populateFromModel();
        }
    }


    private Component buildContent() {

        Div layout = new Div();
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
    private void updateIcon(byte[] imageData) {
        Image img = utils.byteArrayToImage(imageData);
        imgPlaceholder.removeAll();
        img.setWidth(3f, Unit.EM);
        img.setHeight(3f, Unit.EM);
        imgPlaceholder.add(img);
    }


    private Component buildBody() {

        Div body = new Div();
        body.addClassName("body");

        Tab tab1 = new Tab("General");
        Component page1 = buildPage1();

        Tab tab2 = new Tab("Permutations");
        Component page2 = buildPage2();
        page2.setVisible(false);


        Map<Tab, Component> tabsToPages = new HashMap<>();
        tabsToPages.put(tab1, page1);
        tabsToPages.put(tab2, page2);
        Tabs tabs = new Tabs(tab1, tab2);
        Div pages = new Div(page1, page2);

        tabs.addSelectedChangeListener(event -> {
            tabsToPages.values().forEach(page -> page.setVisible(false));
            Component selectedPage = tabsToPages.get(tabs.getSelectedTab());
            selectedPage.setVisible(true);
        });

        body.add(tabs, pages);

        return body;
    }

    private Component buildPage1(){
        FlexLayout layout = new FlexLayout();
        layout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);

        imgPlaceholder=new FlexLayout();
        imgPlaceholder.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        imgPlaceholder.getStyle().set("margin-top","auto");
        Resource res=context.getResource(Application.GENERIC_INDEX_ICON);
        byte[] imageData;
        try {
            imageData = Files.readAllBytes(Paths.get(res.getURI()));
            imageData = utils.scaleImage(imageData, MAX_IMG_WIDTH, MAX_IMG_HEIGHT);
            updateIcon(imageData);
        } catch (IOException e) {
            e.printStackTrace();
        }
        buildCombo();
        FlexLayout comboPanel = new FlexLayout();
        comboPanel.setFlexDirection(FlexLayout.FlexDirection.ROW);
        comboPanel.getStyle().set("gap","1em");
        comboPanel.add(imgPlaceholder, indexCombo);

        startDatePicker=new DatePicker("Start date");
        startDatePicker.setMaxWidth("10em");
        startDatePicker.setRequired(true);

        amountFld = new NumberField("Amount");
        amountFld.setWidth("6em");
        leverageFld=new IntegerField("Leverage");
        leverageFld.setWidth("6em");
        stopLossFld= new NumberField("SL");
        stopLossFld.setWidth("6em");
        takeProfitFld= new NumberField("TP");
        takeProfitFld.setWidth("6em");
        FlexLayout amountsLayout = new FlexLayout();
        amountsLayout.getStyle().set("gap","1em");
        amountsLayout.setFlexDirection(FlexLayout.FlexDirection.ROW);
        amountsLayout.add(amountFld, leverageFld, stopLossFld, takeProfitFld);

        lengthRadioGroup = new RadioButtonGroup<>();
        lengthRadioGroup.setLabel("Simulation length");
        lengthRadioGroup.setItems("Fixed", "Variable");
        lengthRadioGroup.addValueChangeListener(event -> {
            if (event.getValue() != null) {
                numberOfDays.setVisible(event.getValue().equals("Fixed"));
            }
        });

        numberOfDays = new IntegerField("Number of days");
        numberOfDays.setLabel("Number of days");
        numberOfDays.setHasControls(true);
        numberOfDays.setMin(2);

        FlexLayout lengthLayout = new FlexLayout();
        lengthLayout.getStyle().set("gap","1em");
        lengthLayout.setFlexDirection(FlexLayout.FlexDirection.ROW);
        lengthLayout.add(lengthRadioGroup, numberOfDays);

        layout.add(comboPanel, startDatePicker, amountsLayout, lengthLayout);

        return layout;
    }

    private Component buildPage2(){

        FlexLayout layout = new FlexLayout();
        layout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);

        Component amplitudePanel = buildAmplitudePanel();
        Component avgDaysPanel = buildAvgDaysPanel();

        layout.add(amplitudePanel, avgDaysPanel);

        return layout;
    }

    private Component buildAmplitudePanel(){
        FlexLayout layout = new FlexLayout();
        layout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);

        amplitudeFld = new IntegerField("Amplitude %");

        amplitudeMinFld = new IntegerField("Amplitude min %");
        amplitudeMaxFld = new IntegerField("Amplitude max %");
        amplitudeStepsFld = new IntegerField("# of steps");
        FlexLayout amplitudeLayout = new FlexLayout();
        amplitudeLayout.setFlexDirection(FlexLayout.FlexDirection.ROW);
        amplitudeLayout.getStyle().set("gap","1em");
        amplitudeLayout.add(amplitudeMinFld, amplitudeMaxFld, amplitudeStepsFld);
        amplitudeLayout.setVisible(false);

        permutateAmplitudeCheckbox = new Checkbox();
        permutateAmplitudeCheckbox.setLabel("Permutate amplitude");
        permutateAmplitudeCheckbox.addValueChangeListener((HasValue.ValueChangeListener<AbstractField.ComponentValueChangeEvent<Checkbox, Boolean>>) event -> {
            boolean checked = event.getValue();
            amplitudeFld.setVisible(!checked);
            amplitudeLayout.setVisible(checked);
        });

        layout.add(amplitudeFld, amplitudeLayout, permutateAmplitudeCheckbox);

        return layout;
    }

    private Component buildAvgDaysPanel(){

        FlexLayout layout = new FlexLayout();
        layout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);

        avgDaysFld = new IntegerField("days lookback");
        avgDaysMinFld = new IntegerField("days lookback min");
        avgDaysMaxFld = new IntegerField("days lookback max");
        avgDaysStepsFld = new IntegerField("# of steps");
        FlexLayout avgDaysLayout = new FlexLayout();
        avgDaysLayout.setFlexDirection(FlexLayout.FlexDirection.ROW);
        avgDaysLayout.getStyle().set("gap","1em");
        avgDaysLayout.add(avgDaysMinFld, avgDaysMaxFld, avgDaysStepsFld);
        avgDaysLayout.setVisible(false);

        permutateAvgDaysCheckbox = new Checkbox();
        permutateAvgDaysCheckbox.setLabel("Permutate days lookback");
        permutateAvgDaysCheckbox.addValueChangeListener((HasValue.ValueChangeListener<AbstractField.ComponentValueChangeEvent<Checkbox, Boolean>>) event -> {
            boolean checked = event.getValue();
            avgDaysFld.setVisible(!checked);
            avgDaysLayout.setVisible(checked);
        });

        layout.add(avgDaysFld, avgDaysLayout, permutateAvgDaysCheckbox);

        return layout;

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
            Div divSymbol = new Div();
            divSymbol.setText(item.getSymbol());
            divSymbol.getStyle().set("font-weight", "bold");
            Div divName = new Div();
            divName.setText(item.getName());
            divName.setMaxHeight("0.6em");
            divName.getStyle().set("font-size", "60%");
            FlexLayout texts = new FlexLayout();
            texts.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
            texts.add(divSymbol, divName);
            texts.getStyle().set("margin-left", "0.5em");

            Image image = utils.byteArrayToImage(item.getImage());
            image.setWidth("2em");
            image.setHeight("2em");

            FlexLayout wrapper = new FlexLayout();
            wrapper.setFlexDirection(FlexLayout.FlexDirection.ROW);
            wrapper.add(image, texts);

            return wrapper;
        });

        indexCombo = new ComboBox<>();
        indexCombo.setLabel("Index");
        indexCombo.setWidth("14em");
        indexCombo.setDataProvider(dataProvider);
        indexCombo.setRenderer(listItemRenderer);
        indexCombo.setItemLabelGenerator(MarketIndex::getSymbol);
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
        //indexCombo.setValue(model.get);
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


}
