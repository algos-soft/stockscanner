package com.algos.stockscanner.views.permutate;

import com.algos.stockscanner.beans.Utils;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.PageTitle;
import com.algos.stockscanner.views.main.MainView;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.component.dependency.CssImport;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

@Route(value = "permutate", layout = MainView.class)
@RouteAlias(value = "", layout = MainView.class)
@PageTitle("Permutate")
@CssImport("./views/permutate/permutate-view.css")
public class PermutateView extends Div {

    private @Autowired Utils utils;

    public PermutateView() {
        addClassName("permutate-view");
        add(new Text("Content placeholder"));

        // customize the header
        addAttachListener((ComponentEventListener<AttachEvent>) attachEvent -> {
            Optional<Component> parent = getParent();
            if(parent.isPresent()){
                Optional<HorizontalLayout> customArea = utils.findCustomArea(parent.get());
                if(customArea.isPresent()){
                    customArea.get().removeAll();
                    //customizeHeader(customArea.get());
                }
            }
        });

    }

}
