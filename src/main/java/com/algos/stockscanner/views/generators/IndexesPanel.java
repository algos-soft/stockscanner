package com.algos.stockscanner.views.generators;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
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
@CssImport(value = "./views/generators/indexespanel.css")
@org.springframework.stereotype.Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class IndexesPanel extends HorizontalLayout {

    private List<IndexComponent> indexComponents;
    private Component emptyView;

    @PostConstruct
    private void init(){
        setSpacing(false);
        setPadding(false);

        Div div = new Div();
        div.setText("No indexes - click to add");
        div.addClassName("emptyview");
        emptyView=div;

        addClassName("indexespanel");

        indexComponents=new ArrayList<>();

        syncEmptyView();

    }

    public void add(IndexComponent indexComponent) {
        indexComponents.add(indexComponent);
        syncEmptyView();
        super.add(indexComponent);
    }

    @Override
    public void removeAll() {
        indexComponents.clear();
        super.removeAll();
        syncEmptyView();
    }

    public List<IndexComponent> getIndexComponents() {
        return indexComponents;
    }

    private void syncEmptyView(){
        if(indexComponents.size()==0){
            super.add(emptyView);
        }else{
            super.remove(emptyView);
        }
    }

}
