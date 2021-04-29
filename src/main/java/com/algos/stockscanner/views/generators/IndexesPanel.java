package com.algos.stockscanner.views.generators;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * Component showing multiple IndexComponent(s)
 * (index images with symbol)
 */
@org.springframework.stereotype.Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class IndexesPanel extends HorizontalLayout {

    private List<IndexComponent> indexComponents;

    @PostConstruct
    private void init(){
        setSpacing(true);
        setPadding(false);
        indexComponents=new ArrayList<>();
    }

    public void add(IndexComponent indexComponent) {
        indexComponents.add(indexComponent);
        super.add(indexComponent);
    }

    public List<IndexComponent> getIndexComponents() {
        return indexComponents;
    }
}
