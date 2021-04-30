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
import java.util.List;

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

    public IndexPicker(List<Integer> selectedIds) {
        this.selectedIds=selectedIds;
    }

    @PostConstruct
    private void init(){
        setSpacing(false);
        setPadding(true);

        addClassName("indexpicker");
        populateBody();
    }


    private void populateBody(){
        List<MarketIndex> entities = marketIndexService.findAllOrderBySymbol();
        for(MarketIndex entity : entities){
            IndexModel model = new IndexModel();
            marketIndexService.entityToModel(entity, model);
            PickerItem comp = context.getBean(PickerItem.class, utils.toPrimitive(model.getId()), model.getImage(), model.getSymbol());
            comp.addClickListener(new ComponentEventListener<ClickEvent<VerticalLayout>>() {
                @Override
                public void onComponentEvent(ClickEvent<VerticalLayout> event) {
                    FlexComponent fComp=event.getSource();
                    if(isHighlighted(fComp)){
                        dim(fComp);
                    }else{
                        highlight(fComp);
                    }
                }
            });

            if(selectedIds.contains(model.getId())){
                highlight(comp);
            }

            add(comp);
        }
    }

    private void highlight(FlexComponent item){
        Style style = item.getStyle();
        style.set("background","#d8952a");
        style.set("filter","invert(100%)");
    }

    private void dim(FlexComponent item){
        Style style = item.getStyle();
        style.remove("background");
        style.remove("filter");
    }

    private boolean isHighlighted(FlexComponent item){
        Style style = item.getStyle();
        String value = style.get("filter");
        return value!=null;
    }



}
