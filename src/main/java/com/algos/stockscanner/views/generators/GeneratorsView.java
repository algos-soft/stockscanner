package com.algos.stockscanner.views.generators;

import com.algos.stockscanner.beans.Utils;
import com.algos.stockscanner.data.entity.Generator;
import com.algos.stockscanner.data.entity.MarketIndex;
import com.algos.stockscanner.data.entity.Simulation;
import com.algos.stockscanner.data.service.GeneratorService;
import com.algos.stockscanner.data.service.MarketIndexService;
import com.algos.stockscanner.views.simulations.SimulationModel;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.IronIcon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.router.*;
import com.algos.stockscanner.views.main.MainView;
import com.vaadin.flow.component.dependency.CssImport;
import org.claspina.confirmdialog.ButtonOption;
import org.claspina.confirmdialog.ConfirmDialog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Route(value = "generators", layout = MainView.class)
@RouteAlias(value = "", layout = MainView.class)
@PageTitle("Generators")
@CssImport(value="./views/generators/generators-view.css")
@CssImport(value="./views/generators/generators-grid.css", themeFor = "vaadin-grid")
public class GeneratorsView extends Div implements AfterNavigationObserver  {

    private Grid<GeneratorModel> grid;

    private String filtSymbol;

    private Example<Generator> filter;


    @Autowired
    private GeneratorService generatorService;

    @Autowired
    private MarketIndexService marketIndexService;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private Utils utils;

    public GeneratorsView() {
    }

    @PostConstruct
    private void init() {

        addClassName("generators-view");
        setSizeFull();

//        grid = new Grid<>();
//        grid.setHeight("100%");
//        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_NO_ROW_BORDERS);
//        grid.addComponentColumn(index -> createCard(index));
        //add(grid);

        filter=Example.of(new Generator());    // empty initial filter

        createGrid();
        Component filterPanel = createFilterPanel();

        VerticalLayout layout = new VerticalLayout();
        layout.getStyle().set("height","100%");
        layout.add(filterPanel, grid);

        add(layout);


        // customize the header
        addAttachListener((ComponentEventListener<AttachEvent>) attachEvent -> {
            Optional<Component> parent = getParent();
            if (parent.isPresent()) {
                Optional<HorizontalLayout> customArea = utils.findCustomArea(parent.get());
                if (customArea.isPresent()) {
                    customArea.get().removeAll();
                    customizeHeader(customArea.get());
                }
            }
        });


    }




    /**
     * Reload data when this view is displayed.
     */
    @Override
    public void afterNavigation(AfterNavigationEvent event) {
    }

    private void customizeHeader(HorizontalLayout header){

        header.getStyle().set("flex-direction","row-reverse");

        Button addButton = new Button("New Generator",  new Icon(VaadinIcon.PLUS_CIRCLE));
        addButton.getStyle().set("margin-left","1em");
        addButton.getStyle().set("margin-right","1em");
        addButton.setIconAfterText(true);
        addButton.addClickListener((ComponentEventListener<ClickEvent<Button>>) buttonClickEvent -> {
            addNewItem();
        });

        header.add(addButton);
    }


    private void createGrid(){
        grid = new Grid<>();
        grid.setHeight("100%");
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_NO_ROW_BORDERS);
        grid.addComponentColumn(index -> createCard(index));

        CallbackDataProvider<GeneratorModel, Void> provider;
        provider = DataProvider.fromCallbacks(fetchCallback -> {
            int offset = fetchCallback.getOffset();
            int limit = fetchCallback.getLimit();
            List<QuerySortOrder> sorts = fetchCallback.getSortOrders();
            return generatorService.fetch(offset, limit, filter, sorts).stream();
        }, countCallback -> {
            return generatorService.count(filter);
        });

        grid.setDataProvider(provider);


    }

    private Component createFilterPanel(){

        ComboBox<MarketIndex> indexCombo = utils.buildIndexCombo();
        indexCombo.addValueChangeListener(new HasValue.ValueChangeListener<AbstractField.ComponentValueChangeEvent<ComboBox<MarketIndex>, MarketIndex>>() {
            @Override
            public void valueChanged(AbstractField.ComponentValueChangeEvent<ComboBox<MarketIndex>, MarketIndex> event) {
                filtSymbol=null;
                MarketIndex marketIndex = event.getValue();
                if(marketIndex!=null){
                    filtSymbol = marketIndex.getSymbol();
                }
                filter();
            }
        });

        HorizontalLayout layout = new HorizontalLayout();
        layout.add(indexCombo);
        return layout;
    }



    private HorizontalLayout createCard(GeneratorModel model) {

        HorizontalLayout card = new HorizontalLayout();
        card.addClassName("card");
        card.setSpacing(false);
        card.getThemeList().add("spacing-s");

        Component pan1=buildPan1(model);
        Component pan2= buildPan2(model);
        Component pan3= buildPan3(model);
        Component pan4= buildPan4(model);
        Component action = buildActionCombo(model);

        card.add(pan1, pan2, pan3, pan4, action);

        return card;
    }


    private Component buildPan1(GeneratorModel model){

        IronIcon tagIcon = new IronIcon("vaadin", "tag");

        int number = utils.toPrimitive(model.getNumber());
        Span sNumber = new Span(""+number);
        sNumber.addClassName("number");

        HorizontalLayout row1 = new HorizontalLayout();
        row1.addClassName("tagRow");
        row1.add(tagIcon, sNumber);

        Image img = model.getImage();
        if(img==null){
            img = utils.byteArrayToImage(utils.getDefaultIndexIcon());
        }
        img.addClassName("icon");

        Span symbol = new Span(model.getSymbol());
        symbol.addClassName("symbol");
        HorizontalLayout hl = new HorizontalLayout();
        hl.add(img, symbol);

        Pan pan = new Pan();
        pan.add(row1, hl);
        return pan;
    }


    private Component buildPan2(GeneratorModel model){

        int amount = utils.toPrimitive(model.getAmount());
        Span cAmount = new Span(format(amount));
        cAmount.addClassName("amount");
        int leverage = utils.toPrimitive(model.getLeverage());
        Span cLeverage = new Span("X"+format(leverage));
        cLeverage.addClassName("leverage");
        HorizontalLayout row1 = new HorizontalLayout();
        row1.add(cAmount, cLeverage);

        int sl = utils.toPrimitive(model.getStopLoss());
        Span cSL=new Span();
        if(sl>0){
            cSL.add("SL "+ format(sl)+"%");
        }
        int tp = utils.toPrimitive(model.getTakeProfit());
        Span cTP=new Span();
        if(tp>0){
            cTP.add("TP "+format(tp)+"%");
        }
        HorizontalLayout row2 = new HorizontalLayout();
        row2.addClassName("sltp");
        row2.add(cSL, cTP);

        Pan pan = new Pan();
        pan.add(row1, row2);

        return pan;
    }


    private Component buildPan3(GeneratorModel model){

        IronIcon calendar = new IronIcon("vaadin", "flag-checkered");
        String sDate;
        if(model.getStartDate()!=null){
            sDate=format(model.getStartDate());
        }else{
            sDate="n.a.";
        }
        Span spanDate = new Span(calendar, new Text(sDate));
        spanDate.addClassName("startdate");

        String period;
        IronIcon durationIcon = new IronIcon("vaadin", "clock");
        if(model.isDurationFixed()){
            period=model.getDays()+" days fixed";
        }else{
            if(model.getDays()>0){
                period="max "+model.getDays()+" days";
            }else{
                period="unlimited";
            }
        }
        Span spanPeriod = new Span(durationIcon, new Text(period));
        spanPeriod.addClassName("period");

        String sSpan;
        IronIcon spanIcon = new IronIcon("vaadin", "refresh");
        if(model.getSpans()>1){
            sSpan=model.getSpans()+" spans";
        }else{
            sSpan="single span";
        }
        Span spanSpan = new Span(spanIcon, new Span(sSpan));
        spanSpan.addClassName("spans");

        Pan pan = new Pan();
        pan.add(spanDate, spanPeriod, spanSpan);
        return pan;
    }


    private Component buildPan4(GeneratorModel model){

        IronIcon amplIcon = new IronIcon("vaadin", "arrows-long-v");
        String sAmplitude;
        if(model.isPermutateAmpitude()){
            sAmplitude=model.getAmplitudeMin()+"% - "+model.getAmplitudeMax()+"%, in "+model.getAmplitudeSteps()+" steps";
        }else{
            sAmplitude=model.getAmplitude()+"%";
        }
        Span spanAmplitude = new Span(amplIcon, new Text(sAmplitude));
        spanAmplitude.addClassName("amplitude");

        IronIcon lookIcon = new IronIcon("vaadin", "glasses");
        String sLook;
        if(model.isPermutateDaysLookback()){
            sLook=model.getDaysLookbackMin()+" - "+model.getDaysLookbackMax()+" days, in "+model.getDaysLookbackSteps()+" steps";
        }else{
            sLook=model.getDaysLookback()+" days";
        }
        Span spanLook = new Span(lookIcon, new Text(sLook));
        spanLook.addClassName("lookback");

        Pan pan = new Pan();
        pan.add(spanAmplitude, spanLook);
        return pan;

    }

    /**
     * base for card panels
     */
    class Pan extends VerticalLayout{
        public Pan() {
            setSpacing(false);
            setPadding(false);
            getThemeList().add("spacing-s");
            addClassName("panel");
        }
    }

    private String format(LocalDate d){
        if(d!=null){
            return d.format(DateTimeFormatter.ofPattern("dd MMM u"));
        }
        return null;
    }

    private String format(Integer n){
        if(n!=null){
            return String.format("%,d", n);
        }
        return null;
    }


    /**
     * Present an empty dialog to create a new item
     */
    private void addNewItem(){

        GeneratorDialogConfirmListener listener =  new GeneratorDialogConfirmListener() {
            @Override
            public void onConfirm(GeneratorModel model) {
                Generator entity = new Generator();
                generatorService.initEntity(entity);
                generatorService.modelToEntity(model, entity);
                generatorService.update(entity);
                refreshGrid();
            }
        };

        GeneratorModel model = new GeneratorModel();
        generatorService.initModel(model);  // set defaults
        GeneratorDialog dialog = context.getBean(GeneratorDialog.class, model, listener);

        dialog.open();
    }




    /**
     * Transform Entity to view Model
     */
    private GeneratorModel createModel(Generator entity) {
        GeneratorModel m = new GeneratorModel();
        generatorService.entityToModel(entity, m);
        return m;
    }


    private Component buildActionCombo(GeneratorModel model){

        MenuBar menuBar = new MenuBar();
        MenuItem account = menuBar.addItem("Actions...");


        // edit an item
        account.getSubMenu().addItem("Run generator", i -> {
        });

        // show results
        account.getSubMenu().addItem("Show results", i -> {
        });

        // edit an item
        account.getSubMenu().addItem("Edit generator", i -> {

            Generator entity = generatorService.get(model.getId()).get();

            GeneratorDialogConfirmListener listener = model1 -> {
                generatorService.modelToEntity(model, entity);
                generatorService.update(entity);    // write db
                generatorService.entityToModel(entity, model1); // from db back to model - to be sure the model reflects the changes happened on db
                grid.getDataProvider().refreshItem(model1); // refresh only this item
            };

            GeneratorDialog dialog = context.getBean(GeneratorDialog.class, model, listener);

            dialog.open();

        });

        // Delete an Index
        account.getSubMenu().addItem("Delete generator", i -> {

            Button bConfirm = new Button();
            ConfirmDialog dialog = ConfirmDialog.create().withMessage("Do you want to delete "+model.getNumber()+" - "+model.getSymbol()+"?")
                    .withButton(new Button(), ButtonOption.caption("Cancel"), ButtonOption.closeOnClick(true))
                    .withButton(bConfirm, ButtonOption.caption("Delete"), ButtonOption.focus(), ButtonOption.closeOnClick(true));

            bConfirm.addClickListener((ComponentEventListener<ClickEvent<Button>>) event1 -> {
                try {
                    generatorService.delete(model.getId());
                    refreshGrid();
                }catch (Exception e){
                    e.printStackTrace();
                }
            });

            dialog.open();
        });

        // edit an item
        account.getSubMenu().addItem("Clone generator", i -> {
        });

        return menuBar;

    }



    /**
     * update the current filter
     */
    private void filter(){
        Generator entity = new Generator();


        if(filtSymbol!=null){
            MarketIndex marketIndex = new MarketIndex();
            marketIndex.setSymbol(filtSymbol);
            entity.setIndex(marketIndex);
        }

        filter = Example.of(entity);

        refreshGrid();

    }

    private void refreshGrid() {
        grid.select(null);
        grid.getDataProvider().refreshAll();
    }




}
