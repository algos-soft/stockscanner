package com.algos.stockscanner.views.indexes;

import com.algos.stockscanner.beans.Utils;
import com.algos.stockscanner.exceptions.InvalidBigNumException;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@CssImport(value = "./views/indexes/filter-panel.css")
public class FilterPanel extends FlexLayout {

    private TextField nameFld;
    private TextField exchangeFld;
    private TextField countryFld;
    private TextField sectorFld;
    private TextField industryFld;
    private RangeFld marketCapRange;
    private RangeFld ebitdaRange;
    private Button bSearch;

    private List<FilterPanelListener> listeners=new ArrayList<>();

    @Autowired
    private Utils utils;


    @PostConstruct
    private void init(){

        nameFld=new TextField("name|symbol");
        //nameFld.setClearButtonVisible(true);
        exchangeFld=new TextField("exchange");
        //exchangeFld.setClearButtonVisible(true);
        countryFld=new TextField("country");
        //countryFld.setClearButtonVisible(true);
        sectorFld=new TextField("sector");
        //sectorFld.setClearButtonVisible(true);
        industryFld=new TextField("industry");
        //industryFld.setClearButtonVisible(true);
        marketCapRange=new RangeFld("cap");
        //marketCapRange.setClearButtonVisible(true);
        ebitdaRange=new RangeFld("ebitda");
        //ebitdaRange.setClearButtonVisible(true);
        bSearch=new Button("Search");
        bSearch.addClickListener((ComponentEventListener<ClickEvent<Button>>) buttonClickEvent -> fireSearchPressed());

        buildUI();
    }


    private void buildUI(){
        addClassName("filter-panel");
        nameFld.addClassName("filter-panel-field");
        exchangeFld.addClassName("filter-panel-field");
        countryFld.addClassName("filter-panel-field");
        sectorFld.addClassName("filter-panel-field");
        industryFld.addClassName("filter-panel-field");
        bSearch.addClassName("filter-panel-search-button");
        add(nameFld, exchangeFld, countryFld, sectorFld, industryFld, marketCapRange, ebitdaRange, bSearch);
    }


    public IndexFilter buildFilter() throws InvalidBigNumException {
        IndexFilter filter = new IndexFilter();
        filter.symbol=nameFld.getValue();
        filter.name=nameFld.getValue();
        filter.exchange=exchangeFld.getValue();
        filter.country=countryFld.getValue();
        filter.sector=sectorFld.getValue();
        filter.industry=industryFld.getValue();
        filter.marketCapFrom=marketCapRange.getFromValue();
        filter.marketCapFrom=marketCapRange.getToValue();
        filter.ebitdaFrom=ebitdaRange.getFromValue();
        filter.ebitdaFrom=ebitdaRange.getToValue();
        return filter;
    }


    class RangeFld extends FlexLayout {
        private String label;
        private TextField fromFld;
        private TextField toFld;

        public RangeFld(String label) {
            this.label = label;
            fromFld=new TextField(label+" from");
            fromFld.getElement().setAttribute("title", "0.0 M/G/T");    // tooltip
            fromFld.addValueChangeListener((HasValue.ValueChangeListener<AbstractField.ComponentValueChangeEvent<TextField, String>>) event -> {
                toFld.setValue(event.getValue());
            });
            toFld=new TextField("to");
            toFld.getElement().setAttribute("title", "0.0 M/G/T");    // tooltip
            buildUI();
        }

        private void buildUI(){
            addClassName("filter-panel-range");
            fromFld.addClassName("filter-panel-range-field1");
            Icon arrow = new Icon(VaadinIcon.CHEVRON_RIGHT_SMALL);
            arrow.addClassName("filter-panel-range-arrow");
            toFld.addClassName("filter-panel-range-field2");
            add(fromFld, arrow, toFld);
        }

        private void setClearButtonVisible(boolean flag){
            fromFld.setClearButtonVisible(flag);
            toFld.setClearButtonVisible(flag);
        }

        public long getFromValue() throws InvalidBigNumException {
            return utils.convertBigNum(fromFld.getValue());
        }

        public long getToValue() throws InvalidBigNumException {
            return utils.convertBigNum(toFld.getValue());
        }

    }

    public void addListener(FilterPanelListener listener){
        listeners.add(listener);
    }

    private void fireSearchPressed(){
        for(FilterPanelListener listener : listeners){
            listener.searchButtonPressed();
        }
    }

    public interface FilterPanelListener{
        void searchButtonPressed();
    }


}
