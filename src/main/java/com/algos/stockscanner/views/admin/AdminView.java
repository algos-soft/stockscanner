package com.algos.stockscanner.views.admin;

import com.algos.stockscanner.beans.Utils;
import com.algos.stockscanner.data.service.AdminService;
import com.algos.stockscanner.views.main.MainView;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

@Route(value = "admin", layout = MainView.class)
@PageTitle("Admin")
@CssImport("./views/admin/admin-view.css")
public class AdminView extends VerticalLayout {

    private static final String MARKET_INDEXES = "Market Indexes";
    private static final String GENERATOR = "Generator";

    private Div placeholder;
    private Component marketIndexesComponent;
    private Component generatorComponent;

    private @Autowired
    Utils utils;

    public AdminView(AdminService adminService) {
        addClassName("admin-view");
    }

    @PostConstruct
    private void init() {

        Select<String> selector = new Select<>();
        selector.setItems(MARKET_INDEXES, GENERATOR);
        selector.setLabel("Topic");
        selector.addValueChangeListener(new HasValue.ValueChangeListener<AbstractField.ComponentValueChangeEvent<Select<String>, String>>() {
            @Override
            public void valueChanged(AbstractField.ComponentValueChangeEvent<Select<String>, String> event) {
                switch (event.getValue()) {
                    case MARKET_INDEXES:
                        placeholder.removeAll();
                        if(marketIndexesComponent==null){
                            marketIndexesComponent=buildMarketIndexesComponent();
                        }
                        placeholder.add(marketIndexesComponent);
                        break;
                    case GENERATOR:
                        placeholder.removeAll();
                        if(generatorComponent==null){
                            generatorComponent=buildGeneratorComponent();
                        }
                        placeholder.add(generatorComponent);
                        break;
                }
            }
        });

        placeholder = new Div();
        add(selector, placeholder);
    }


    private Component buildMarketIndexesComponent(){

        Button bDownloadIndexes = new Button("Download indexes");
        Button bUpdateAllIndexData = new Button("Update data for all indexes");

        VerticalLayout layout = new VerticalLayout();
        //layout.getStyle().set("background","yellow");
        layout.add(bDownloadIndexes, bUpdateAllIndexData);

        return layout;

    }


    private Component buildGeneratorComponent(){

        Button bDownloadIndexes = new Button("Test");

        VerticalLayout layout = new VerticalLayout();
        //layout.getStyle().set("background","yellow");
        layout.add(bDownloadIndexes);

        return layout;

    }


}
