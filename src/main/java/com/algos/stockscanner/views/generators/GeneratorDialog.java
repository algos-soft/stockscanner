package com.algos.stockscanner.views.generators;

import com.algos.stockscanner.beans.Utils;
import com.algos.stockscanner.data.entity.MarketIndex;
import com.algos.stockscanner.data.service.MarketIndexService;
import com.algos.stockscanner.views.indexes.IndexModel;
import com.vaadin.flow.component.*;
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
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.IronIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.IntegerField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.Resource;

import javax.annotation.PostConstruct;
import javax.persistence.criteria.CriteriaBuilder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Dialog to edit a Generator
 */
@CssImport("./views/generators/generators-dialog.css")
@org.springframework.stereotype.Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class GeneratorDialog extends Dialog {

    private static final String LABEL_FIXED = "Fixed";
    private static final String LABEL_VARIABLE = "Variable";

    private GeneratorModel model;
    private GeneratorDialogConfirmListener confirmListener;

    private FlexLayout imgPlaceholder;

    private ComboBox<MarketIndex> indexComboBox;
    private DatePicker startDatePicker;

    private IntegerField amountFld;
    private IntegerField stopLossFld;
    private IntegerField takeProfitFld;

    private RadioButtonGroup<String> lengthRadioGroup;
    private IntegerField numberOfDays;
    private IntegerField numberOfSpans;

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

    private Checkbox permutateIndexesCheckbox;
    private IndexCombo indexCombo;
    private IndexesPanel indexesPanel;

    @Autowired
    private Utils utils;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private MarketIndexService marketIndexService;


    public GeneratorDialog() {
        this((GeneratorDialogConfirmListener) null);
    }

    public GeneratorDialog(GeneratorDialogConfirmListener confirmListener) {
        this((GeneratorModel) null, confirmListener);
        this.model = model;
    }

    public GeneratorDialog(GeneratorModel model, GeneratorDialogConfirmListener confirmListener) {
        this.model = model;
        this.confirmListener = confirmListener;
    }


    @PostConstruct
    private void init() {
        setWidth("30em");
        setHeight("42em");
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


        // PROVVISORIO!!
        Button button = new Button("associate indexes");
        button.addClickListener(new ComponentEventListener<ClickEvent<Button>>() {
            @Override
            public void onComponentEvent(ClickEvent<Button> buttonClickEvent) {
                List<MarketIndex> entities = marketIndexService.findAll();
                for(MarketIndex entity : entities){
                    IndexModel model = new IndexModel();
                    marketIndexService.entityToModel(entity, model);
                    IndexComponent indexComponent=context.getBean(IndexComponent.class, utils.toPrimitive(model.getId()), model.getImage(), model.getSymbol());
                    indexesPanel.add(indexComponent);
                }
            }
        });
        layout.add(button);

        return layout;

    }


    private Component buildHeader() {
        Div header = new Div();
        header.addClassName("header");

        // load default icon
        Resource res = context.getResource("images/generator.png");
        byte[] imageData = null;
        try {
            imageData = Files.readAllBytes(Paths.get(res.getURI()));
//            imageData = utils.scaleImage(imageData, MAX_IMG_WIDTH, MAX_IMG_HEIGHT);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Image img = utils.byteArrayToImage(imageData);
        img.setWidth(2, Unit.EM);
        img.setHeight(2, Unit.EM);

        Label title = new Label("Generator");
        header.add(img, title);
        return header;
    }

    /**
     * Updates the icon in the header based on the current byte array
     * <p>
     * null image data restores the default icon
     *
     */
    private void updateIcon(byte[] imageData) {
        imgPlaceholder.removeAll();
        if(imageData==null){
            imageData=utils.getDefaultIndexIcon();
        }
        Image img = utils.byteArrayToImage(imageData);
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
        byte[] imageData=utils.getDefaultIndexIcon();
        updateIcon(imageData);
        buildCombo();
        FlexLayout comboPanel = new FlexLayout();
        comboPanel.setFlexDirection(FlexLayout.FlexDirection.ROW);
        comboPanel.getStyle().set("gap","1em");
        comboPanel.add(imgPlaceholder, indexComboBox);

        IronIcon tagIcon = new IronIcon("vaadin", "tag");
        Span sNumber = new Span(""+utils.toPrimitive(model.getNumber()));
        sNumber.addClassName("tagnumber");
        HorizontalLayout tagLayout=new HorizontalLayout();
        tagLayout.addClassName("taglayout");
        tagLayout.add(tagIcon, sNumber);

        HorizontalLayout row1=new HorizontalLayout();
        row1.add(comboPanel, tagLayout);

        startDatePicker=new DatePicker("Start date");
        startDatePicker.setMaxWidth("10em");
        startDatePicker.setRequired(true);

        amountFld = new IntegerField("Amount");
        amountFld.setWidth("6em");

        stopLossFld= new IntegerField("SL%");
        stopLossFld.setWidth("6em");
        stopLossFld.setHelperText("for each cycle");

        takeProfitFld= new IntegerField("TP%");
        takeProfitFld.setWidth("6em");
        takeProfitFld.setHelperText("for each cycle");
        //takeProfitFld.getElement().setAttribute("tooltip", "for each cycle");

        FlexLayout amountsLayout = new FlexLayout();
        amountsLayout.getStyle().set("gap","1em");
        amountsLayout.setFlexDirection(FlexLayout.FlexDirection.ROW);
        amountsLayout.add(amountFld, stopLossFld, takeProfitFld);

        lengthRadioGroup = new RadioButtonGroup<>();
        lengthRadioGroup.setLabel("Simulation length");
        lengthRadioGroup.setItems(LABEL_FIXED, LABEL_VARIABLE);
        lengthRadioGroup.addValueChangeListener(event -> {
            if (event.getValue() != null) {
                if(event.getValue().equals(LABEL_FIXED)){
                    numberOfDays.setLabel("Fixed number of days");
                    numberOfDays.setHelperText("The simulation will stop after this number of days");
                }else{
                    numberOfDays.setLabel("Max number of days");
                    numberOfDays.setHelperText("(Optional) the simulation will not exceed this number of days");
                }
            }
        });

        numberOfDays = new IntegerField("Number of days");
        numberOfDays.setLabel("Number of days");
        numberOfDays.setHasControls(true);
        numberOfDays.setMin(2);
        numberOfDays.setWidth("10em");

        numberOfSpans = new IntegerField("Number of spans");
        numberOfSpans.setLabel("Number of spans");
        numberOfSpans.setHasControls(true);
        numberOfSpans.setMin(1);
        numberOfSpans.setWidth("10em");
        numberOfSpans.setHelperText("Number of repetitions, each starting when the previous ended");

        FlexLayout lengthLayout = new FlexLayout();
        lengthLayout.getStyle().set("gap","1em");
        lengthLayout.setFlexDirection(FlexLayout.FlexDirection.ROW);
        lengthLayout.add(numberOfDays, numberOfSpans);

        layout.add(row1, startDatePicker, amountsLayout, lengthRadioGroup, lengthLayout);

        return layout;
    }

    private Component buildPage2(){

        VerticalLayout layout = new VerticalLayout();

        Component indexPanel = buildIndexPanel();
        Component amplitudePanel = buildAmplitudePanel();
        Component avgDaysPanel = buildAvgDaysPanel();

        layout.add(indexPanel, amplitudePanel, avgDaysPanel);

        return layout;
    }


    private Component buildIndexPanel(){
        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(false);
        layout.setPadding(false);
        indexCombo=context.getBean(IndexCombo.class);
        indexesPanel=context.getBean(IndexesPanel.class);
        permutateIndexesCheckbox = new Checkbox("Permutate indexes");
        indexesPanel.setVisible(false);

        permutateIndexesCheckbox.addValueChangeListener((HasValue.ValueChangeListener<AbstractField.ComponentValueChangeEvent<Checkbox, Boolean>>) event -> {
            boolean checked = event.getValue();
            indexCombo.setVisible(!checked);
            indexesPanel.setVisible(checked);
        });

        layout.add(indexCombo,indexesPanel,permutateIndexesCheckbox);
        return layout;
    }


    private Component buildAmplitudePanel(){
        VerticalLayout layout = new VerticalLayout();

        amplitudeFld = new IntegerField("Amplitude %");

        amplitudeMinFld = new IntegerField("Amplitude min %");
        amplitudeMaxFld = new IntegerField("Amplitude max %");
        amplitudeStepsFld = new IntegerField("# of steps");
        HorizontalLayout amplitudeLayout = new HorizontalLayout();
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

        indexComboBox =utils.buildIndexCombo();
        indexComboBox.setRequired(true);
        indexComboBox.addValueChangeListener(event -> {
            MarketIndex index = event.getValue();
            byte[] imageData=null;
            if (index!=null){
                imageData=index.getImage();
            }
            updateIcon(imageData);
        });

    }


    private Component buildFooter() {

        Div btnLayout = new Div();
        btnLayout.addClassName("footer");

        Button confirmButton = new Button("Confirm", event -> {
            GeneratorModel model = modelFromDialog();
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
    private GeneratorModel modelFromDialog() {

        GeneratorModel model;
        if (this.model != null) {
            model = this.model;
        } else {
            model = new GeneratorModel();
        }

        MarketIndex index = indexComboBox.getValue();
        if(index!=null){
            model.setSymbol(index.getSymbol());
        }

        // replace index model from Indexes panel contents
        List<IndexComponent> indexComponents = indexesPanel.getIndexComponents();
        model.getIndexes().clear();
        for(IndexComponent indexComponent : indexComponents){
            int indexId=indexComponent.getIndexId();
            MarketIndex marketIndex;
            if(indexId>0){
                marketIndex=marketIndexService.get(indexId).get();
                IndexModel indexModel = new IndexModel();
                marketIndexService.entityToModel(marketIndex,indexModel);
                model.getIndexes().add(indexModel);
            }
        }

        model.setStartDate(startDatePicker.getValue());
        model.setAmount(utils.toPrimitive(amountFld.getValue()));
        model.setStopLoss(utils.toPrimitive(stopLossFld.getValue()));
        model.setTakeProfit(utils.toPrimitive(takeProfitFld.getValue()));

        String value=lengthRadioGroup.getValue();
        if(value!=null){
            model.setDurationFixed(value.equals(LABEL_FIXED));
        }

        model.setDays(utils.toPrimitive(numberOfDays.getValue()));
        model.setSpans(utils.toPrimitive(numberOfSpans.getValue()));

        model.setAmplitude(utils.toPrimitive(amplitudeFld.getValue()));
        model.setAmplitudeMin(utils.toPrimitive(amplitudeMinFld.getValue()));
        model.setAmplitudeMax(utils.toPrimitive(amplitudeMaxFld.getValue()));
        model.setAmplitudeSteps(utils.toPrimitive(amplitudeStepsFld.getValue()));
        model.setPermutateAmpitude(permutateAmplitudeCheckbox.getValue());

        model.setDaysLookback(utils.toPrimitive(avgDaysFld.getValue()));
        model.setDaysLookbackMin(utils.toPrimitive(avgDaysMinFld.getValue()));
        model.setDaysLookbackMax(utils.toPrimitive(avgDaysMaxFld.getValue()));
        model.setDaysLookbackSteps(utils.toPrimitive(avgDaysStepsFld.getValue()));
        model.setPermutateDaysLookback(permutateAvgDaysCheckbox.getValue());

        model.setPermutateIndexes(permutateIndexesCheckbox.getValue());

        return model;
    }


    private void populateFromModel() {

        if(model.getSymbol()!=null){
            try {
                MarketIndex index=marketIndexService.findUniqueBySymbol(model.getSymbol());
                indexComboBox.setValue(index);
                updateIcon(index.getImage());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        indexesPanel.removeAll();
        for(IndexModel iModel : model.getIndexes()){
            IndexComponent indexComponent = context.getBean(IndexComponent.class, iModel.getId(), iModel.getImage(), iModel.getSymbol());
            indexesPanel.add(indexComponent);
        }

        startDatePicker.setValue(model.getStartDate());
        amountFld.setValue(utils.toPrimitive(model.getAmount()));
        stopLossFld.setValue(utils.toPrimitive(model.getStopLoss()));
        takeProfitFld.setValue(utils.toPrimitive(model.getTakeProfit()));
        if(model.isDurationFixed()){
            lengthRadioGroup.setValue(LABEL_FIXED);
        }else{
            lengthRadioGroup.setValue(LABEL_VARIABLE);
        }
        numberOfDays.setValue(utils.toPrimitive(model.getDays()));
        numberOfSpans.setValue(utils.toPrimitive(model.getSpans()));

        amplitudeFld.setValue(utils.toPrimitive(model.getAmplitude()));
        amplitudeMinFld.setValue(utils.toPrimitive(model.getAmplitudeMin()));
        amplitudeMaxFld.setValue(utils.toPrimitive(model.getAmplitudeMax()));
        amplitudeStepsFld.setValue(utils.toPrimitive(model.getAmplitudeSteps()));
        permutateAmplitudeCheckbox.setValue(utils.toPrimitive(model.isPermutateAmpitude()));

        avgDaysFld.setValue(utils.toPrimitive(model.getDaysLookback()));
        avgDaysMinFld.setValue(utils.toPrimitive(model.getDaysLookbackMin()));
        avgDaysMaxFld.setValue(utils.toPrimitive(model.getDaysLookbackMax()));
        avgDaysStepsFld.setValue(utils.toPrimitive(model.getDaysLookbackSteps()));
        permutateAvgDaysCheckbox.setValue(utils.toPrimitive(model.isPermutateDaysLookback()));

        permutateIndexesCheckbox.setValue(utils.toPrimitive(model.isPermutateIndexes()));

    }


}
