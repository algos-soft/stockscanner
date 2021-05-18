package com.algos.stockscanner.views.indexes;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
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
    private IntRange marketCapRange;
    private IntRange ebitdaRange;

    @PostConstruct
    private void init(){

        nameFld=new TextField("name|symbol");
        nameFld.addClassName("filter-panel-field");

        exchangeFld=new TextField("exchange");
        exchangeFld.addClassName("filter-panel-field");

        countryFld=new TextField("country");
        countryFld.addClassName("filter-panel-field");

        sectorFld=new TextField("sector");
        sectorFld.addClassName("filter-panel-field");

        industryFld=new TextField("industry");
        industryFld.addClassName("filter-panel-field");

        marketCapRange=new IntRange("industry");
        marketCapRange.addClassName("filter-panel-field");

        ebitdaRange=new IntRange("ebitda");
        ebitdaRange.addClassName("filter-panel-field");

        buildUI();
    }


    private void buildUI(){
        addClassName("filter-panel");
        add(nameFld, exchangeFld, countryFld, sectorFld, industryFld);
    }

    class IntRange extends HorizontalLayout{
        private String label;

        public IntRange(String label) {
            this.label = label;
        }
    }

}
