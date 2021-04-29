package com.algos.stockscanner.views.generators;

import com.vaadin.flow.component.dialog.Dialog;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

import javax.annotation.PostConstruct;

@org.springframework.stereotype.Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class IndexPicker extends Dialog {

    @PostConstruct
    private void init(){

    }
}
