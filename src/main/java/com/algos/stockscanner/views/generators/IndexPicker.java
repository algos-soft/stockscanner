package com.algos.stockscanner.views.generators;

import com.algos.stockscanner.beans.Utils;
import com.algos.stockscanner.data.entity.MarketIndex;
import com.algos.stockscanner.data.service.MarketIndexService;
import com.algos.stockscanner.views.indexes.IndexModel;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.dom.Style;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Component to display all the available indexes
 * allows choosing multiple items through checkboxes
 */
@CssImport(value = "./views/generators/indexpicker.css")
@org.springframework.stereotype.Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class IndexPicker extends HorizontalLayout {

    @Autowired
    private MarketIndexService marketIndexService;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private Utils utils;

    private List<Integer> selectedIds;

    private Map<PickerItem, Boolean> pickersMap;

    public IndexPicker(List<Integer> selectedIds, IndexPickerSelectionListener selectionListener) {
        this.selectedIds=selectedIds;
        this.selectionListener = selectionListener;
    }

    private String filter;

    private IndexPickerSelectionListener selectionListener;

    @PostConstruct
    private void init(){
        setSpacing(false);
        setPadding(true);

        pickersMap=new HashMap<>();

        addClassName("indexpicker");
        populateBody();
    }


    private void populateBody(){
        List<MarketIndex> entities = marketIndexService.findAllOrderBySymbol();
        for(MarketIndex entity : entities){
            IndexModel model = new IndexModel();

            marketIndexService.entityToModel(entity, model);
            PickerItem pickerItem = context.getBean(PickerItem.class, utils.toPrimitive(model.getId()), model.getImage(), model.getSymbol());

            pickerItem.addClickListener((ComponentEventListener<ClickEvent<VerticalLayout>>) event -> {
                Component fComp=event.getSource();
                PickerItem item = (PickerItem)fComp;
                if(isHighlighted(item)){
                    dim(item);
                    pickersMap.put(item, false);
                    selectionListener.itemSelection(item, false, countSelected());
                }else{
                    highlight(item);
                    pickersMap.put(item, true);
                    selectionListener.itemSelection(item, true, countSelected());
                }
            });

            boolean selected=false;
            if(selectedIds.contains(model.getId())){
                highlight(pickerItem);
                selected=true;
            }

            add(pickerItem);
            pickersMap.put(pickerItem, selected);

            if(selected){
                selectionListener.itemSelection(pickerItem, true, countSelected());
            }

        }
    }

    private void highlight(PickerItem item){
        Style style = item.getStyle();
        style.set("background","#d8952a");
        style.set("filter","invert(100%)");
    }

    private void dim(PickerItem item){
        Style style = item.getStyle();
        style.remove("background");
        style.remove("filter");
    }

    private boolean isHighlighted(PickerItem item){
        Style style = item.getStyle();
        String value = style.get("filter");
        return value!=null;
    }


    public List<Integer> getSelectedIds() {
        List<Integer> selectedIds=new ArrayList<>();
        for(Map.Entry<PickerItem, Boolean> entry :pickersMap.entrySet()){
            PickerItem item = entry.getKey();
            Boolean selected = entry.getValue();
            if(selected){
                selectedIds.add(item.getIndexId());
            }
        }
        return selectedIds;
    }

    public void filter(String value) {
        this.filter=value;

        for(Map.Entry<PickerItem, Boolean> entry :pickersMap.entrySet()){
            PickerItem item = entry.getKey();
            String symbol=item.getIndexSymbol();
            if(symbol.toUpperCase().contains(value.toUpperCase())){
                item.setVisible(true);
            }else{
                item.setVisible(false);
            }

        }
    }


    private int countSelected(){
        int count=0;
        for(boolean selected :pickersMap.values() ){
            if(selected){
                count++;
            }
        }
        return count;
    }



}
