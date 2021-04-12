package com.algos.stockscanner.runner;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@Scope("prototype")
public class GeneratorMonitor extends HorizontalLayout {

    @PostConstruct
    private void init(){
        setWidth("10em");
        setHeight("2em");
        getStyle().set("background","yellow");
    }

}
