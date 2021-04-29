package com.algos.stockscanner.views.generators;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

import javax.annotation.PostConstruct;

/**
 * Component showing Index image and combo chooser
 */
@org.springframework.stereotype.Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class IndexCombo extends HorizontalLayout {

    @PostConstruct
    private void init(){

    }

}
