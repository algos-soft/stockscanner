package com.algos.stockscanner.utils;


import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import java.util.Optional;
import java.util.stream.Stream;

@org.springframework.stereotype.Component
public class Utils {

    /**
     * Find a child component by id
     */
    public Optional<Component> findChildById(Component parent, String id){
        Stream<Component> children = parent.getChildren();
        Stream<Component> filtered = children.filter(b -> {
            if(b.getId().isPresent()){
                return b.getId().get().equalsIgnoreCase(id);
            }
            return false;
        });
        return filtered.findFirst();
    }

    /**
     * Find the customizable area of the header inside the main view
     */
    public Optional<HorizontalLayout> findCustomArea(Component mainView){
        Optional<Component> header = findChildById(mainView,"header");
        if(header.isPresent()){
            Optional<Component> custom = findChildById(header.get(),"custom");
            if(custom.isPresent() && custom.get() instanceof HorizontalLayout){
                HorizontalLayout hl = (HorizontalLayout)custom.get();
                Optional<HorizontalLayout> opt = Optional.of(hl);
                return opt;
            }
        }
        return null;
    }


}
