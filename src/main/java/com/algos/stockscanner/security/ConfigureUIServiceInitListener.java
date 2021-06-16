package com.algos.stockscanner.security;

import com.vaadin.flow.component.*;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.*;
import org.springframework.stereotype.Component;

/**
 * Project vaadwam
 * Created by Algos
 * User: gac
 * Date: mer, 21-ago-2019
 * Time: 21:45
 */
@Component
public class ConfigureUIServiceInitListener implements VaadinServiceInitListener {

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource().addUIInitListener(uiEvent -> {
            final UI ui = uiEvent.getUI();
            ui.addBeforeEnterListener(this::beforeEnter); //


        });
    }

    /**
     * Reroutes the user if (s)he is not authorized to access the view.
     *
     * @param event before navigation event with event details
     */
    private void beforeEnter(BeforeEnterEvent event) {
        //        if (!LoginView.class.equals(event.getNavigationTarget()) //
        //
        //
        //                && !SecurityUtils.isUserLoggedIn()) { //
        //
        //
        //            event.rerouteTo(LoginView.class); //
        //
        //
        //        }
        //    }
    }

}
