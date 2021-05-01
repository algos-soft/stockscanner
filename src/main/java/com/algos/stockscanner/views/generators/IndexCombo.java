package com.algos.stockscanner.views.generators;

import com.algos.stockscanner.beans.Utils;
import com.algos.stockscanner.data.entity.MarketIndex;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

import javax.annotation.PostConstruct;

/**
 * Component showing Index image and combo chooser
 */
@CssImport(value = "./views/generators/indexcombo.css")
@org.springframework.stereotype.Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class IndexCombo extends HorizontalLayout {

    @Autowired
    private Utils utils;

    private FlexLayout imgPlaceholder;
    private ComboBox<MarketIndex> indexComboBox;

    @PostConstruct
    private void init(){
        setSpacing(true);
        setPadding(false);
        addClassName("indexcombo");

        imgPlaceholder=new FlexLayout();
        imgPlaceholder.addClassName("imgplaceholder");

        //imgPlaceholder.getStyle().set("margin-top","auto");

        buildCombo();

        byte[] imageData=utils.getDefaultIndexIcon();
        updateIcon(imageData);

        add(imgPlaceholder, indexComboBox);

    }



    private void buildCombo() {

        indexComboBox =utils.buildIndexCombo();
        indexComboBox.addClassName("combobox");
        indexComboBox.setRequired(true);
        indexComboBox.addValueChangeListener(event -> {
            MarketIndex index = event.getValue();
            byte[] imageData=null;
            if (index!=null){
                imageData=index.getImage();
            }
            updateIcon(imageData);
        });

    }



    /**
     * Updates the icon in the header based on the current byte array
     * <p>
     * null image data restores the default icon
     *
     */
    private void updateIcon(byte[] imageData) {
        imgPlaceholder.removeAll();
        if(imageData==null){
            imageData=utils.getDefaultIndexIcon();
        }
        Image img = utils.byteArrayToImage(imageData);
        img.addClassName("image");
//        img.setWidth(3f, Unit.EM);
//        img.setHeight(3f, Unit.EM);
        imgPlaceholder.add(img);
    }


    public void setValue(MarketIndex index) {
        indexComboBox.setValue(index);
        updateIcon(index.getImage());
    }

    public MarketIndex getValue() {
        return indexComboBox.getValue();
    }
}
