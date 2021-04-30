package com.algos.stockscanner.views.generators;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

import javax.annotation.PostConstruct;

/**
 * Component showing Index image and symbol
 */
@CssImport(value = "./views/generators/indexcomponent.css")
@org.springframework.stereotype.Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class IndexComponent extends VerticalLayout {

    private int indexId;
    private Image indexImage;
    private String indexSymbol;

    public IndexComponent(int indexId, Image indexImage, String indexSymbol) {
        this.indexId = indexId;
        this.indexImage = indexImage;
        this.indexSymbol = indexSymbol;
    }

    @PostConstruct
    private void init(){
        setSpacing(false);
        setPadding(false);

        addClassName("indexcomponent");

        indexImage.addClassName("image");

        Div symbolDiv = new Div();
        symbolDiv.addClassName("symbol");
        symbolDiv.setText(indexSymbol);

        add(indexImage, symbolDiv);
    }

    public int getIndexId() {
        return indexId;
    }

}