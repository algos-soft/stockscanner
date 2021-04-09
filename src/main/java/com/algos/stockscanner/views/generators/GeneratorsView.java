package com.algos.stockscanner.views.generators;

import com.algos.stockscanner.beans.Utils;
import com.algos.stockscanner.data.entity.Generator;
import com.algos.stockscanner.data.entity.MarketIndex;
import com.algos.stockscanner.data.service.GeneratorService;
import com.algos.stockscanner.data.service.MarketIndexService;
import com.algos.stockscanner.services.MarketService;
import com.algos.stockscanner.views.indexes.IndexDialog;
import com.algos.stockscanner.views.indexes.IndexDialogConfirmListener;
import com.algos.stockscanner.views.indexes.IndexModel;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.IronIcon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.router.*;
import com.algos.stockscanner.views.main.MainView;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.server.Command;
import org.claspina.confirmdialog.ButtonOption;
import org.claspina.confirmdialog.ConfirmDialog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Route(value = "generators", layout = MainView.class)
@RouteAlias(value = "", layout = MainView.class)
@PageTitle("Generators")
@CssImport("./views/generators/generators-view.css")
public class GeneratorsView extends Div implements AfterNavigationObserver  {

    Grid<GeneratorModel> grid = new Grid<>();

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
        grid.setHeight("100%");
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_NO_ROW_BORDERS);
        grid.addComponentColumn(index -> createCard(index));
        add(grid);

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
        loadAll();
    }

    private void customizeHeader(HorizontalLayout header){

        Button addButton = new Button("New Generator",  new Icon(VaadinIcon.PLUS_CIRCLE));
        addButton.getStyle().set("margin-left","1em");
        addButton.getStyle().set("margin-right","1em");
        addButton.setIconAfterText(true);
        addButton.addClickListener((ComponentEventListener<ClickEvent<Button>>) buttonClickEvent -> {
            addNewItem();
        });

        header.add(addButton);
    }



    private HorizontalLayout createCard(GeneratorModel model) {

        HorizontalLayout card = new HorizontalLayout();
        card.addClassName("card");
        card.setSpacing(false);
        card.getThemeList().add("spacing-s");

        Component pan1=buildPan1(model);
        Component pan2=buildPan2(model);
        Component action = buildActionCombo(model);

        card.add(pan1, pan2, action);


//        VerticalLayout body = new VerticalLayout();
//        body.setSpacing(false);
//        body.setPadding(false);
//        body.getThemeList().add("spacing-s");

//        Span symbol = new Span(model.getSymbol());
//        symbol.addClassName("symbol");

//        Span name = new Span(model.getName());
//        name.addClassName("name");

//        String categoryDesc=null;
//        IndexCategories indexCategory=model.getCategory();
//        if (indexCategory!=null){
//            categoryDesc=indexCategory.getDescription();
//        }
//        Span category = new Span(categoryDesc);
//        category.addClassName("category");



//        Div details = new Div();
//
//        IronIcon intervalIcon = new IronIcon("vaadin", "calendar-o");
//        String text;
//        if(model.getNumUnits()>0){
//            text=format(model.getUnitsFrom())+" -> "+format(model.getUnitsTo());
//        }else{
//            text = "no data";
//        }
//        Span intervalSpan = new Span(text);
//        intervalSpan.addClassName("interval");
//
//        IronIcon pointsIcon = new IronIcon("vaadin", "ellipsis-dots-h");
//        Span pointsSpan = new Span(String.format("%,d", model.getNumUnits()));
//        pointsSpan.addClassName("points");
//
//        IronIcon frequencyIcon = new IronIcon("vaadin", "clock");
//        String frequencyDesc=null;
//        FrequencyTypes frequencyType=model.getUnitFrequency();
//        if (frequencyType!=null){
//            frequencyDesc=frequencyType.getDescription();
//        }
//        Span frequancySpan = new Span(frequencyDesc);
//        frequancySpan.addClassName("frequency");
//        details.add(intervalIcon, intervalSpan, pointsIcon, pointsSpan, frequencyIcon, frequancySpan);

        //body.add(symbol, name, category, details);
//        body.add(symbol);

//        Image image = model.getImage();
//        Component action = buildActionCombo(model);

        //card.add(image, body, action);
//        card.add(body);
        return card;
    }


    private Component buildPan1(GeneratorModel model){
        Pan pan = new Pan();
        Image img = model.getImage();
        if(img==null){
            img = utils.byteArrayToImage(utils.getDefaultIndexIcon());
        }
        img.addClassName("icon");

        Span symbol = new Span(model.getSymbol());
        symbol.addClassName("symbol");
        HorizontalLayout hl = new HorizontalLayout();
        hl.add(img, symbol);

        IronIcon calendar = new IronIcon("vaadin", "calendar-o");
        String sDate;
        if(model.getStartDate()!=null){
            sDate=format(model.getStartDate());
        }else{
            sDate="n.a.";
        }
        Span spanDate = new Span(calendar, new Text(sDate));
        spanDate.addClassName("date");

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
        spanPeriod.addClassName("date");



        pan.add(hl, spanDate, spanPeriod);
        return pan;
    }


    private Component buildPan2(GeneratorModel model){
        Pan pan = new Pan();

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

        pan.add(row1, row2);

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
                entity.setCreated(LocalDateTime.now());
                entity.setModified(LocalDateTime.now());
                updateEntity(entity, model);
                generatorService.update(entity);
                loadAll();
            }
        };

        GeneratorDialog dialog = context.getBean(GeneratorDialog.class, listener);

        dialog.open();
    }

    /**
     * Update entity from model
     */
    private void updateEntity(Generator entity, GeneratorModel model){
        String symbol = model.getSymbol();
        MarketIndex index=null;
        try {
            index = marketIndexService.findUniqueBySymbol(symbol);
        } catch (Exception e) {
            e.printStackTrace();
        }
        entity.setIndex(index);

        entity.setStartDate(model.getStartDate());
        entity.setAmount(model.getAmount());
        entity.setLeverage(model.getLeverage());
        entity.setStopLoss(model.getStopLoss());
        entity.setTakeProfit(model.getTakeProfit());
        entity.setFixedDays(model.isDurationFixed());
        entity.setDays(model.getDays());

        entity.setAmplitude(model.getAmplitude());
        entity.setAmplitudeMax(model.getAmplitudeMax());
        entity.setAmplitudeMin(model.getAmplitudeMin());
        entity.setAmplitudeSteps(model.getAmplitudeSteps());
        entity.setAmplitudePermutate(model.isPermutateAmpitude());

        entity.setAvgDays(model.getDaysLookback());
        entity.setAvgDaysMax(model.getDaysLookbackMax());
        entity.setAvgDaysMin(model.getDaysLookbackMin());
        entity.setAvgDaysSteps(model.getDaysLookbackSteps());
        entity.setAvgDaysPermutate(model.isPermutateDaysLookback());
    }

    /**
     * Load all data in the grid
     */
    private void loadAll(){
        List<GeneratorModel> outList=new ArrayList<>();

        Pageable p = Pageable.unpaged();
        Page<Generator> page = generatorService.list(p);

        page.stream().forEach(e -> {
            outList.add(createModel(e));
        });

        grid.setItems(outList);
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

        // edit an item
        account.getSubMenu().addItem("Edit generator", i -> {

            Generator entity = generatorService.get(model.getId()).get();

            GeneratorDialogConfirmListener listener = model1 -> {
                updateEntity(entity, model1);
                generatorService.update(entity);    // write db
                generatorService.entityToModel(entity, model1); // from db back to model - to be sure model is aligned with db
                grid.getDataProvider().refreshItem(model1); // refresh only this item
            };

            GeneratorDialog dialog = context.getBean(GeneratorDialog.class, model, listener);

            dialog.open();

        });

        // Delete an Index
        account.getSubMenu().addItem("Delete generator", i -> {

            Button bConfirm = new Button();
            ConfirmDialog dialog = ConfirmDialog.create().withMessage("Do you want to delete "+model.getSymbol()+"?")
                    .withButton(new Button(), ButtonOption.caption("Cancel"), ButtonOption.closeOnClick(true))
                    .withButton(bConfirm, ButtonOption.caption("Delete"), ButtonOption.focus(), ButtonOption.closeOnClick(true));

            bConfirm.addClickListener((ComponentEventListener<ClickEvent<Button>>) event1 -> {
                try {
                    generatorService.delete(model.getId());
                    loadAll();
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




}
