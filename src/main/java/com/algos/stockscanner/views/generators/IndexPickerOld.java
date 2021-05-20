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
public class IndexPickerOld extends HorizontalLayout {

    @Autowired
    private MarketIndexService marketIndexService;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private Utils utils;

    private List<Integer> selectedIds;

    private List<PickerItem> pickerItems;

    public IndexPickerOld(List<Integer> selectedIds, IndexPickerSelectionListener selectionListener) {
        this.selectedIds=selectedIds;
        this.selectionListener = selectionListener;
    }

    private String filter;

    private IndexPickerSelectionListener selectionListener;

    @PostConstruct
    private void init(){
        setSpacing(false);
        setPadding(true);

        pickerItems=new ArrayList<>();

        addClassName("indexpicker");
        populateBody();
    }


    private void populateBody(){
        List<MarketIndex> entities = marketIndexService.findAllOrderBySymbol();
        for(MarketIndex entity : entities){
            IndexModel model = new IndexModel();

            marketIndexService.entityToModel(entity, model);
            PickerItem pickerItem = context.getBean(PickerItem.class, utils.toPrimitive(model.getId()), model.getImageData(), model.getSymbol());

            pickerItem.addClickListener((ComponentEventListener<ClickEvent<VerticalLayout>>) event -> {
                Component fComp=event.getSource();
                PickerItem item = (PickerItem)fComp;
                if(item.isHighlighted()){
                    item.dim();
                    selectionListener.itemSelection(item, false, countSelected());
                }else{
                    item.highlight();
                    selectionListener.itemSelection(item, true, countSelected());
                }
            });

            boolean selected=false;
            if(selectedIds.contains(model.getId())){
                pickerItem.highlight();
                selected=true;
            }

            add(pickerItem);
            pickerItems.add(pickerItem);

            if(selected){
                selectionListener.itemSelection(pickerItem, true, countSelected());
            }

        }
    }



    public List<Integer> getSelectedIds() {
        List<Integer> selectedIds=new ArrayList<>();
        for(PickerItem item : pickerItems){
            if(item.isHighlighted()){
                selectedIds.add(item.getIndexId());
            }
        }
        return selectedIds;
    }

    public void filter(String value) {
        this.filter=value;

        for(PickerItem item : pickerItems){
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
        for(PickerItem item : pickerItems){
            if(item.isHighlighted()){
                count++;
            }
        }
        return count;
    }


    public void selectAll() {
        for(PickerItem item : pickerItems){
            if(item.isVisible()){
                if(!item.isHighlighted()){
                    item.highlight();
                    selectionListener.itemSelection(item, true, countSelected());
                }
            }
        }
    }

    public void selectNone() {
        for(PickerItem item : pickerItems){
            if(item.isVisible()){
                if(item.isHighlighted()){
                    item.dim();
                    selectionListener.itemSelection(item, false, countSelected());
                }
            }
        }
    }



}
