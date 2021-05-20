package com.algos.stockscanner.views.generators;

import com.algos.stockscanner.beans.Utils;
import com.algos.stockscanner.data.entity.MarketIndex;
import com.algos.stockscanner.data.service.MarketIndexService;
import com.algos.stockscanner.exceptions.InvalidBigNumException;
import com.algos.stockscanner.views.indexes.FilterPanel;
import com.algos.stockscanner.views.indexes.IndexFilter;
//import com.algos.stockscanner.views.indexes.IndexModel;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.AbstractGridSingleSelectionModel;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridMultiSelectionModel;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.selection.SelectionEvent;
import com.vaadin.flow.data.selection.SelectionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Dialog to display and control an IndexPicker
 */
@CssImport("./views/generators/indexpicker-dialog.css")
@org.springframework.stereotype.Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class IndexesPickerDialog extends Dialog  {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private IndexPickerDialogConfirmListener confirmListener;

    @Autowired
    private ApplicationContext context;

    private List<Integer> selectedIds;

    private Grid<MarketIndex> grid;
    private FilterPanel filterPanel;
    private List<QuerySortOrder> order; // current order
    private Label counterLabel;
    private Label selectedLabel;
    private Checkbox onlySelected;

    @Autowired
    private MarketIndexService marketIndexService;
    @Autowired
    private Utils utils;


    public IndexesPickerDialog(IndexPickerDialogConfirmListener confirmListener, List<Integer> selectedIds) {
        this.confirmListener = confirmListener;
        this.selectedIds=selectedIds;
    }


    @PostConstruct
    private void init() {
        setWidth("50em");
        setHeight("40em");
        setCloseOnEsc(false);
        setCloseOnOutsideClick(true);
        setResizable(true);
        setDraggable(true);

        counterLabel=new Label();
        selectedLabel=new Label();

        onlySelected=new Checkbox("show selected only");
        onlySelected.addValueChangeListener(new HasValue.ValueChangeListener<AbstractField.ComponentValueChangeEvent<Checkbox, Boolean>>() {
            @Override
            public void valueChanged(AbstractField.ComponentValueChangeEvent<Checkbox, Boolean> event) {
                grid.getDataProvider().refreshAll();
            }
        });

        createGrid();

        filterPanel = context.getBean(FilterPanel.class);
        filterPanel.addListener(() -> {
            onlySelected.setValue(false);
            grid.getDataProvider().refreshAll();
        });

        add(buildContent());
    }


    private Component buildContent() {
        VerticalLayout layout = new VerticalLayout();
        layout.addClassName("indexpickerdialog-dialog");
        Component header = buildHeader();
        Component body = buildBody();
        Component footer = buildFooter();
        layout.add(header, body, footer);
        return layout;
    }

    private Component buildHeader() {
        Component header = filterPanel;
        return header;
    }



    private Component buildBody() {


        HorizontalLayout layout1=new HorizontalLayout();
        layout1.setAlignItems(FlexComponent.Alignment.BASELINE);
        layout1.add(counterLabel, selectedLabel, onlySelected);

        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setSpacing(false);
        layout.setPadding(false);
        layout.add(layout1, grid);

        Div body = new Div();
        body.addClassName("indexpickerdialog-dialogbody");
        body.add(layout);

        return body;

    }

    private Component buildFooter() {
        HorizontalLayout footer = new HorizontalLayout();
        footer.addClassName("indexpickerdialog-dialogfooter");

        Button confirmButton = new Button("Confirm", event -> {
            Set<MarketIndex> selectedItems=grid.getSelectionModel().getSelectedItems();
            List<Integer> selectedIds=new ArrayList<>();
            selectedItems.stream().forEach(marketIndex -> selectedIds.add(marketIndex.getId()));
            confirmListener.onConfirm(selectedIds);
            close();
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Cancel", event -> {
            close();
        });

        footer.add(cancelButton, confirmButton);

        return footer;
    }





    private void createGrid(){

        grid = new Grid<>();
        grid.setHeight("100%");
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        GridMultiSelectionModel<MarketIndex> selectionModel=(GridMultiSelectionModel<MarketIndex>)grid.getSelectionModel();
        selectionModel.setSelectAllCheckboxVisibility(GridMultiSelectionModel.SelectAllCheckboxVisibility.VISIBLE);
        grid.addSelectionListener((SelectionListener<Grid<MarketIndex>, MarketIndex>) selectionEvent -> {
            int countSelected=selectionEvent.getAllSelectedItems().size();
            selectedLabel.setText("selected: "+countSelected);
        });

        addColumns();

        CallbackDataProvider<MarketIndex, Void> provider;
        provider = DataProvider.fromCallbacks(fetchCallback -> {
            try {
                IndexFilter indexFilter = filterPanel.buildFilter();
                int offset = fetchCallback.getOffset();
                int limit = fetchCallback.getLimit();
                order = fetchCallback.getSortOrders();
                List<MarketIndex> entities = marketIndexService.fetch(offset, limit, indexFilter, order);

                if(onlySelected.getValue()){
                    Set<MarketIndex> selectedItems = grid.getSelectedItems();
                    entities = marketIndexService.fetch(offset, limit, selectedItems, order);
                }

                return entities.stream();
            } catch (InvalidBigNumException e) {
                log.warn(e.getMessage());
                return null;
            }
        }, countCallback -> {
            try {
                IndexFilter indexFilter = filterPanel.buildFilter();
                int count=marketIndexService.count(indexFilter);


                if(onlySelected.getValue()){
//                    int offset = countCallback.getOffset();
//                    int limit = countCallback.getLimit();
//                    List<MarketIndex> entities = marketIndexService.fetch(offset, limit, indexFilter, order);
//                    entities=filterSelectedOnly(entities);
//                    count=entities.size();
                }


                counterLabel.setText(count+" rows");
                return count;
            } catch (InvalidBigNumException e) {
                log.warn(e.getMessage());
                return 0;
            }
        });

        grid.setDataProvider(provider);

        // relesect the previously selected set
        MarketIndex[] items = new MarketIndex[selectedIds.size()];
        for(int i=0; i<items.length; i++ ){
            int id = selectedIds.get(i);
            MarketIndex index = marketIndexService.get(id).get();
            items[i]=index;
        }
        ((GridMultiSelectionModel<MarketIndex>) grid.getSelectionModel()).selectItems(items);

    }


    private void addColumns(){

        Grid.Column<MarketIndex> col;

        // symbol
        col = grid.addComponentColumn(item -> createCol1Component(item)).setHeader("Symbol");
        col.setWidth("10em");
        col = grid.addComponentColumn(item -> createCol2Component(item)).setHeader("exchange/country");
        col.setWidth("6em");
        col = grid.addComponentColumn(item -> createCol3Component(item)).setHeader("sector/industry");
        col.setWidth("14em");
        col = grid.addComponentColumn(item -> createCol4Component(item)).setHeader("cap/ebitda");

    }


    private Component createCol1Component(MarketIndex item){
        byte[] imageData = item.getImage();
        Image img = utils.byteArrayToImage(imageData);
        img.addClassName("indexpickerdialog-image");

        Span spanSymb = new Span(item.getSymbol());
        spanSymb.addClassName("indexpickerdialog-symbol");

        Span spanName = new Span(item.getName());
        spanName.addClassName("indexpickerdialog-name");

        FlexLayout vLayout = new FlexLayout();
        vLayout.addClassName("indexpickerdialog-symbol-name");
        vLayout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        vLayout.add(spanSymb,spanName);

        FlexLayout layout = new FlexLayout();
        layout.setFlexDirection(FlexLayout.FlexDirection.ROW);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        layout.addClassName("indexpickerdialog-col1");
        layout.add(img, vLayout);

        return layout;
    }



    private Component createCol2Component(MarketIndex item){

        Span span1 = new Span(item.getExchange());
        span1.addClassName("indexpickerdialog-exchange");

        Span span2 = new Span(item.getCountry());
        span2.addClassName("indexpickerdialog-country");

        FlexLayout layout = new FlexLayout();
        layout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        layout.addClassName("indexpickerdialog-col2");
        layout.add(span1,span2);

        return layout;
    }


    private Component createCol3Component(MarketIndex item){

        Span span1 = new Span(item.getSector());
        span1.addClassName("indexpickerdialog-sector");

        Span span2 = new Span(item.getIndustry());
        span2.addClassName("indexpickerdialog-industry");

        FlexLayout layout = new FlexLayout();
        layout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        layout.addClassName("indexpickerdialog-col3");
        layout.add(span1,span2);

        return layout;
    }


    private Component createCol4Component(MarketIndex item){

        Span span1 = new Span("cap "+utils.numberWithSuffix(utils.toPrimitive(item.getMarketCap())));
        span1.addClassName("indexpickerdialog-marketcap");

        Span span2 = new Span("ebitda "+utils.numberWithSuffix(utils.toPrimitive(item.getEbitda())));
        span2.addClassName("indexpickerdialog-ebitda");

        FlexLayout layout = new FlexLayout();
        layout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        layout.addClassName("indexpickerdialog-col4");
        layout.add(span1,span2);

        return layout;
    }


    /**
     * Given a list of indexes, returns a new list containing
     * only the ones which are also selected in the list
     */
    private List<MarketIndex> filterSelectedOnly(List<MarketIndex> entities){
        List<MarketIndex> newList=new ArrayList<>();
        for(MarketIndex entity : entities){
            if(grid.getSelectionModel().getSelectedItems().contains(entity)){
                newList.add(entity);
            }
        }
        return newList;
    }

}
