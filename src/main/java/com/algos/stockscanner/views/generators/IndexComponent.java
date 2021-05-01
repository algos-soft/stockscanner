package com.algos.stockscanner.views.generators;

import com.algos.stockscanner.beans.Utils;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Component showing Index image and symbol
 */
@Component
@CssImport(value = "./views/generators/indexcomponent.css")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class IndexComponent extends VerticalLayout  {
    private int indexId;
    private byte[] imageData;
    private Image indexImage;
    private String indexSymbol;
    private String cssSuffix;

    @Autowired
    private Utils utils;

    public IndexComponent(int indexId, byte[] imageData, String indexSymbol, String cssSuffix) {
        this.indexId = indexId;
        this.imageData = imageData;
        this.indexSymbol = indexSymbol;
        this.cssSuffix=cssSuffix;
    }

    public IndexComponent(int indexId, byte[] imageData, String indexSymbol) {
        this(indexId, imageData, indexSymbol, null);
    }

    @PostConstruct
    private void init() {
        if(cssSuffix==null){
            cssSuffix="";
        }

        setSpacing(false);
        setPadding(false);

        addClassName("indexcomponent"+cssSuffix);

        indexImage = utils.byteArrayToImage(imageData);

        indexImage.addClassName("indexcomponent-image"+cssSuffix);

        Div symbolDiv = new Div();
        symbolDiv.addClassName("indexcomponent-symbol"+cssSuffix);
        symbolDiv.setText(indexSymbol);

        add(indexImage, symbolDiv);
    }

    public int getIndexId() {
        return indexId;
    }

    public String getIndexSymbol() {
        return indexSymbol;
    }
}
