package com.algos.stockscanner.views.permutations;

import com.algos.stockscanner.beans.Utils;
import com.algos.stockscanner.data.entity.Permutation;
import com.algos.stockscanner.data.service.PermutationService;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.PageTitle;
import com.algos.stockscanner.views.main.MainView;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.component.dependency.CssImport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Route(value = "permutations", layout = MainView.class)
@RouteAlias(value = "", layout = MainView.class)
@PageTitle("Permutations")
@CssImport("./views/permutations/permutations-view.css")
public class PermutationsView extends Div {

    Grid<PermutationModel> grid = new Grid<>();

    @Autowired
    private PermutationService permutationService;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private Utils utils;

    public PermutationsView() {
    }

    @PostConstruct
    private void init() {
        addClassName("permutations-view");
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


    private void customizeHeader(HorizontalLayout header){

        Button addButton = new Button("Add Permutation",  new Icon(VaadinIcon.PLUS_CIRCLE));
        addButton.getStyle().set("margin-left","1em");
        addButton.getStyle().set("margin-right","1em");
        addButton.setIconAfterText(true);
        addButton.addClickListener((ComponentEventListener<ClickEvent<Button>>) buttonClickEvent -> {
            addNewItem();
        });

        header.add(addButton);
    }



    private HorizontalLayout createCard(PermutationModel model) {

        HorizontalLayout card = new HorizontalLayout();
        card.addClassName("card");
        card.setSpacing(false);
        card.getThemeList().add("spacing-s");

        VerticalLayout body = new VerticalLayout();
        //body.addClassName("description");
        body.setSpacing(false);
        body.setPadding(false);
        body.getThemeList().add("spacing-s");

        Span symbol = new Span(model.getSymbol());
        symbol.addClassName("symbol");

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
        body.add(symbol);

//        Image image = model.getImage();
//        Component action = buildActionCombo(model);

        //card.add(image, body, action);
        card.add(body);
        return card;
    }

    private String format(LocalDate d){
        if(d!=null){
            return d.format(DateTimeFormatter.ofPattern("dd MMM u"));
        }

        return null;
    }

    /**
     * Present an empty dialog to create a new item
     */
    private void addNewItem(){

        PermutationDialogConfirmListener listener =  new PermutationDialogConfirmListener() {
            @Override
            public void onConfirm(PermutationModel model) {
                Permutation entity = new Permutation();
                updateEntity(entity, model);
                permutationService.update(entity);
                loadAll();
            }
        };

        PermutationDialog dialog = context.getBean(PermutationDialog.class, listener);

        dialog.open();
    }

    /**
     * Update entity from model
     */
    private void updateEntity(Permutation entity, PermutationModel model){
//        entity.setImage(model.getImageData());
//        entity.setSymbol(model.getSymbol());
//        entity.setName(model.getName());
//
//        IndexCategories category=model.getCategory();
//        if(category!=null){
//            entity.setCategory(category.getCode());
//        }
//
//        entity.setBuySpreadPercent(model.getBuySpreadPercent());
//        entity.setOvnBuyDay(model.getOvnBuyDay());
//        entity.setOvnBuyWe(model.getOvnBuyWe());
//        entity.setOvnSellDay(model.getOvnSellDay());
//        entity.setOvnSellWe(model.getOvnSellWe());
    }

    /**
     * Load all data in the grid
     */
    private void loadAll(){
        List<PermutationModel> outList=new ArrayList<>();

        Pageable p = Pageable.unpaged();
        Page<Permutation> page = permutationService.list(p);

        page.stream().forEach(e -> {
            outList.add(createModel(e));
        });

        grid.setItems(outList);
    }


    /**
     * Transform Entity to view Model
     */
    private PermutationModel createModel(Permutation entity) {
        PermutationModel m = new PermutationModel();
        permutationService.entityToModel(entity, m);
        return m;
    }






}
