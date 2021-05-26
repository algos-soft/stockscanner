package com.algos.stockscanner.views.generators;

import com.algos.stockscanner.beans.Utils;
import com.algos.stockscanner.data.entity.MarketIndex;
import com.algos.stockscanner.data.service.MarketIndexService;
import com.algos.stockscanner.views.indexes.IndexModel;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.IronIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * Dialog to edit a Generator
 */
@CssImport("./views/generators/generators-dialog.css")
@org.springframework.stereotype.Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class GeneratorDialog extends Dialog {

    private static final Logger log = LoggerFactory.getLogger(GeneratorDialog.class);

    private GeneratorModel model;
    private GeneratorDialogConfirmListener confirmListener;

    private TextField nameFld;

    private DatePicker startDatePicker;

    private IntegerField amountFld;
    private IntegerField stopLossFld;
//    private IntegerField takeProfitFld;

//    private NumberField trendField;


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

    private Component page1;
    private Component page2;

    @Autowired
    private Utils utils;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private MarketIndexService marketIndexService;


//    public GeneratorDialog() {
//        this((GeneratorDialogConfirmListener) null);
//    }

//    public GeneratorDialog(GeneratorDialogConfirmListener confirmListener) {
//        this((GeneratorModel) null, confirmListener);
//        this.model = model;
//    }

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
        setResizable(true);
        setDraggable(true);

        add(buildContent());

        if (model != null) {
            populateFromModel();
        }
    }


    private Component buildContent() {

        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(false);

        layout.addClassName("dialog");
        Component header = buildHeader();
        Component body = buildBody();
        Component footer = buildFooter();
        layout.add(header, body, footer);

        return layout;

    }


    private Component buildHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.addClassName("dialog_header");

        // load default icon
        Image img = new Image("images/generator.png","generator");

        img.setWidth(2, Unit.EM);
        img.setHeight(2, Unit.EM);

        IronIcon tagIcon = new IronIcon("vaadin", "tag");
        Span sNumber = new Span("" + utils.toPrimitive(model.getNumber()));
        sNumber.addClassName("tagnumber");
        HorizontalLayout tagLayout = new HorizontalLayout();
        tagLayout.addClassName("taglayout");
        tagLayout.add(tagIcon, sNumber);


        Label title = new Label("Generator");
        header.add(img, title, tagLayout);
        return header;
    }


    private Component buildBody() {

        page1 = buildPage1();
        page2 = buildPage2();

        Div placeholder = new Div();
        placeholder.getStyle().set("width", "100%");
        placeholder.getStyle().set("height", "100%");
        placeholder.add(page1);

        Tab tab1 = new Tab("General");
        Tab tab2 = new Tab("Permutations");
        Tabs tabs = new Tabs(tab1, tab2);
        tabs.addClassName("dialog_tabs");
        tabs.addSelectedChangeListener((ComponentEventListener<Tabs.SelectedChangeEvent>) event -> {
            Tab tab = event.getSelectedTab();
            placeholder.removeAll();
            if (tab.equals(tab1)) {
                placeholder.add(page1);
            }
            if (tab.equals(tab2)) {
                placeholder.add(page2);
            }
        });

        VerticalLayout body = new VerticalLayout(tabs, placeholder);
        body.addClassName("dialog_body");

        return body;
    }

    private Component buildPage1() {

        nameFld=new TextField("Name");
        nameFld.setWidth("14em");

        startDatePicker = new DatePicker("Start date");
        startDatePicker.setMaxWidth("10em");
        startDatePicker.setRequired(true);

        amountFld = new IntegerField("Amount");
        amountFld.setWidth("6em");

        stopLossFld = new IntegerField("SL%");
        stopLossFld.setWidth("6em");
        stopLossFld.setHelperText("for each cycle");

//        trendField=new NumberField("Trend");
//        trendField.setValue(0d);
//        trendField.setWidth("9em");
//        trendField.setHelperText("-1=-45°, 0=flat, 1=+45°");
//        trendField.setHasControls(true);
//        trendField.setStep(0.1d);
//        trendField.setMin(-1);
//        trendField.setMax(1);


//        takeProfitFld = new IntegerField("TP%");
//        takeProfitFld.setWidth("6em");
//        takeProfitFld.setHelperText("for each cycle");

        HorizontalLayout amountsLayout = new HorizontalLayout();
        amountsLayout.setSpacing(true);
        amountsLayout.add(amountFld, stopLossFld);


        numberOfDays = new IntegerField("Number of days");
        numberOfDays.setLabel("Number of days");
        numberOfDays.setHasControls(true);
        numberOfDays.setMin(2);
        numberOfDays.setWidth("10em");
        numberOfDays.setHelperText("The simulation will stop after this number of days or when you reach SL");


        numberOfSpans = new IntegerField("Number of spans");
        numberOfSpans.setLabel("Number of spans");
        numberOfSpans.setHasControls(true);
        numberOfSpans.setMin(1);
        numberOfSpans.setWidth("10em");
        numberOfSpans.setHelperText("Number of consecutive repetitions");

        HorizontalLayout lengthLayout = new HorizontalLayout();
        lengthLayout.setSpacing(true);
        lengthLayout.add(numberOfDays, numberOfSpans);

        HorizontalLayout row1=new HorizontalLayout();
        row1.add(nameFld, startDatePicker);


        


        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(false);
        layout.setPadding(false);
        layout.add(row1, amountsLayout, lengthLayout);

        return layout;
    }

    private Component buildPage2() {

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(false);
        layout.setPadding(false);

        Component indexPanel = buildIndexPanel();
        Component amplitudePanel = buildAmplitudePanel();
        Component avgDaysPanel = buildAvgDaysPanel();

        layout.add(indexPanel, amplitudePanel, avgDaysPanel);

        return layout;
    }


    private Component buildIndexPanel() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(false);
        layout.setPadding(false);

        indexCombo = context.getBean(IndexCombo.class);
        indexesPanel = context.getBean(IndexesPanel.class);
        permutateIndexesCheckbox = new Checkbox("Permutate indexes");
        indexesPanel.setVisible(false);


        // when the dialog is confirmed, replace the contents of the indexes panel
        IndexPickerDialogConfirmListener listener = selectedIds -> {
            List<IndexComponent> components = new ArrayList<>();
            for (int id : selectedIds) {
                MarketIndex entity = marketIndexService.get(id).get();
                IndexModel indexModel = new IndexModel();
                marketIndexService.entityToModel(entity, indexModel);
                IndexComponent indexComponent = context.getBean(IndexComponent.class, indexModel.getId(), indexModel.getImageData(), indexModel.getSymbol());
                components.add(indexComponent);
            }

            // sort by symbol
            sortIndexComponents(components);

            // add
            indexesPanel.removeAll();
            for (IndexComponent comp : components) {
                indexesPanel.add(comp);
            }


        };

        Button chooseButton = new Button("Choose...");
        chooseButton.addClickListener((ComponentEventListener<ClickEvent<Button>>) event -> {
            // build a list of the ids of the indexes contained in the IndexesPanel
            List<IndexComponent> indexComponents = indexesPanel.getIndexComponents();
            List<Integer> ids = new ArrayList<>();
            for (IndexComponent comp : indexComponents) {
                ids.add(comp.getIndexId());
            }

            // open the dialog
            IndexesPickerDialog dialog = context.getBean(IndexesPickerDialog.class, listener, ids);
            dialog.open();

        });


//        // add a click listener to the indexesPanel to open the
//        // indexes picker dialog when is clicked
//        indexesPanel.addClickListener(new ComponentEventListener<ClickEvent<HorizontalLayout>>() {
//            @Override
//            public void onComponentEvent(ClickEvent<HorizontalLayout> horizontalLayoutClickEvent) {
//
//                // build a list of the ids of the indexes contained in the IndexesPanel
//                List<IndexComponent> indexComponents = indexesPanel.getIndexComponents();
//                List<Integer> ids = new ArrayList<>();
//                for (IndexComponent comp : indexComponents) {
//                    ids.add(comp.getIndexId());
//                }
//                // open the dialog
//                IndexesPickerDialog dialog = context.getBean(IndexesPickerDialog.class, listener, ids);
//                dialog.open();
//            }
//        });


        permutateIndexesCheckbox.addValueChangeListener((HasValue.ValueChangeListener<AbstractField.ComponentValueChangeEvent<Checkbox, Boolean>>) event -> {
            boolean checked = event.getValue();
            indexCombo.setVisible(!checked);
            indexesPanel.setVisible(checked);
            chooseButton.setVisible(checked);
        });

        // chechbox and button
        HorizontalLayout layout1=new HorizontalLayout();
        layout1.setAlignItems(FlexComponent.Alignment.BASELINE);
        layout1.add(permutateIndexesCheckbox, chooseButton);

        layout.add(indexCombo, indexesPanel, layout1);
        return layout;
    }


    private Component buildAmplitudePanel() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(false);
        layout.setPadding(false);

        amplitudeFld = new IntegerField("Amplitude %");

        amplitudeMinFld = new IntegerField("Amplitude min %");
        amplitudeMaxFld = new IntegerField("Amplitude max %");
        amplitudeStepsFld = new IntegerField("# of steps");
        HorizontalLayout amplitudeLayout = new HorizontalLayout();
        amplitudeLayout.setSpacing(true);
        amplitudeLayout.setPadding(false);
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

    private Component buildAvgDaysPanel() {

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(false);
        layout.setPadding(false);

        avgDaysFld = new IntegerField("moving average");
        avgDaysMinFld = new IntegerField("moving avg min");
        avgDaysMaxFld = new IntegerField("moving avg max");
        avgDaysStepsFld = new IntegerField("# of steps");
        HorizontalLayout avgDaysLayout = new HorizontalLayout();
        avgDaysLayout.setSpacing(true);
        avgDaysLayout.setPadding(false);
        avgDaysLayout.add(avgDaysMinFld, avgDaysMaxFld, avgDaysStepsFld);
        avgDaysLayout.setVisible(false);

        permutateAvgDaysCheckbox = new Checkbox();
        permutateAvgDaysCheckbox.setLabel("Permutate moving average");
        permutateAvgDaysCheckbox.addValueChangeListener((HasValue.ValueChangeListener<AbstractField.ComponentValueChangeEvent<Checkbox, Boolean>>) event -> {
            boolean checked = event.getValue();
            avgDaysFld.setVisible(!checked);
            avgDaysLayout.setVisible(checked);
        });

        layout.add(avgDaysFld, avgDaysLayout, permutateAvgDaysCheckbox);

        return layout;

    }


    private Component buildFooter() {

        HorizontalLayout footer = new HorizontalLayout();
        footer.addClassName("dialog_footer");

        Button confirmButton = new Button("Confirm", event -> {
            GeneratorModel model = modelFromDialog();
            confirmListener.onConfirm(model);
            close();
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Cancel", event -> {
            close();
        });

        footer.add(cancelButton, confirmButton);

        return footer;
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

        MarketIndex index = indexCombo.getValue();
        if (index != null) {
            model.setSymbol(index.getSymbol());
        }

        // replace index model from Indexes panel contents
        List<IndexComponent> indexComponents = indexesPanel.getIndexComponents();
        model.getIndexes().clear();
        for (IndexComponent indexComponent : indexComponents) {
            int indexId = indexComponent.getIndexId();
            MarketIndex marketIndex;
            if (indexId > 0) {
                marketIndex = marketIndexService.get(indexId).get();
                IndexModel indexModel = new IndexModel();
                marketIndexService.entityToModel(marketIndex, indexModel);
                model.getIndexes().add(indexModel);
            }
        }

        model.setName(nameFld.getValue());
        model.setStartDate(startDatePicker.getValue());
        model.setAmount(utils.toPrimitive(amountFld.getValue()));
        model.setStopLoss(utils.toPrimitive(stopLossFld.getValue()));
//        model.setTrend((float)utils.toPrimitive(trendField.getValue()));
//        model.setTakeProfit(utils.toPrimitive(takeProfitFld.getValue()));

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

        if (model.getSymbol() != null) {
            try {
                MarketIndex index = marketIndexService.findUniqueBySymbol(model.getSymbol());
                indexCombo.setValue(index);
            } catch (Exception e) {
                log.error("could not find unique record for symbol "+model.getSymbol(), e);
            }
        }

        // retrieve the indexes
        List<IndexComponent> components = new ArrayList<>();
        for (IndexModel iModel : model.getIndexes()) {
            IndexComponent indexComponent = context.getBean(IndexComponent.class, iModel.getId(), iModel.getImageData(), iModel.getSymbol());
            components.add(indexComponent);
        }

        // sort by symbol
        sortIndexComponents(components);

        // add to the IndexPanel
        indexesPanel.removeAll();
        for (IndexComponent comp : components) {
            indexesPanel.add(comp);
        }

        if(!StringUtils.isEmpty(model.getName())){
            nameFld.setValue(model.getName());
        }
        startDatePicker.setValue(model.getStartDate());
        amountFld.setValue(utils.toPrimitive(model.getAmount()));
        stopLossFld.setValue(utils.toPrimitive(model.getStopLoss()));
//        trendField.setValue((double)utils.toPrimitive(model.getTrend()));
//        takeProfitFld.setValue(utils.toPrimitive(model.getTakeProfit()));
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

    /**
     * Sort a list of IndexComponent(s) by symbol
     */
    private void sortIndexComponents(List<IndexComponent> components) {
        Collections.sort(components, new Comparator<IndexComponent>() {
            @Override
            public int compare(IndexComponent comp1, IndexComponent comp2) {
                return comp1.getIndexSymbol().compareTo(comp2.getIndexSymbol());
            }
        });
    }

}
