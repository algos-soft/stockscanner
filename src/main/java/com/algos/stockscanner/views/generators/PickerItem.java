package com.algos.stockscanner.views.generators;

import com.algos.stockscanner.beans.Utils;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.dom.Style;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

import javax.annotation.PostConstruct;

/**
 * Selectable component showing Index image and symbol
 */
@CssImport(value = "./views/generators/pickeritem.css")
@org.springframework.stereotype.Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PickerItem extends VerticalLayout {

    private int indexId;
    private byte[] imageData;
    private Image indexImage;
    private String indexSymbol;
    private boolean highlighted;
    private Div symbolDiv;

    @Autowired
    private Utils utils;

    public PickerItem(int indexId, byte[] imageData, String indexSymbol) {
        this.indexId = indexId;
        this.imageData = imageData;
        this.indexSymbol = indexSymbol;
    }

    @PostConstruct
    private void init(){
        setSpacing(false);
        setPadding(false);
        addClassName("pickeritem");

        indexImage=utils.byteArrayToImage(imageData);

        indexImage.addClassName("image");

        symbolDiv = new Div();
        symbolDiv.addClassName("symbol");
        symbolDiv.setText(indexSymbol);

        add(indexImage, symbolDiv);
    }


    public void highlight(){
        Style style = getStyle();
        style.set("background","#3399ff");
        symbolDiv.getStyle().set("color","white");
        symbolDiv.getStyle().set("font-weight","bold");
        //style.set("filter","invert(100%)");
        highlighted=true;
    }

    public void dim(){
        Style style = getStyle();
        style.remove("background");
        symbolDiv.getStyle().remove("color");
        symbolDiv.getStyle().remove("font-weight");
        //style.remove("filter");
        highlighted=false;
    }

    public boolean isHighlighted(){
        return highlighted;
    }


    public int getIndexId() {
        return indexId;
    }

    public String getIndexSymbol() {
        return indexSymbol;
    }
}
