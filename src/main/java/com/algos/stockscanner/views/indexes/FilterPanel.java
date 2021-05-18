package com.algos.stockscanner.views.indexes;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.textfield.TextField;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

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

    @PostConstruct
    private void init(){

        nameFld=new TextField("name|symbol");
        exchangeFld=new TextField("exchange");
        countryFld=new TextField("country");
        sectorFld=new TextField("sector");
        industryFld=new TextField("industry");
        marketCapRange=new RangeFld("industry");
        ebitdaRange=new RangeFld("ebitda");

        buildUI();
    }


    private void buildUI(){
        addClassName("filter-panel");
        nameFld.addClassName("filter-panel-field");
        exchangeFld.addClassName("filter-panel-field");
        countryFld.addClassName("filter-panel-field");
        sectorFld.addClassName("filter-panel-field");
        industryFld.addClassName("filter-panel-field");
        add(nameFld, exchangeFld, countryFld, sectorFld, industryFld, marketCapRange, ebitdaRange);
    }


    class RangeFld extends FlexLayout {
        private String label;
        private TextField fromFld;
        private TextField toFld;

        public RangeFld(String label) {
            this.label = label;
            fromFld=new TextField("from");
            toFld=new TextField("to");
            buildUI();
        }

        private void buildUI(){
            addClassName("filter-panel-range");
            fromFld.addClassName("filter-panel-range-field");
            Icon arrow = new Icon(VaadinIcon.CHEVRON_RIGHT_SMALL);
            arrow.addClassName("filter-panel-range-arrow");
            toFld.addClassName("filter-panel-range-field");
            add(fromFld, arrow, toFld);
        }
    }

}
