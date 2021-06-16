package com.algos.stockscanner.security;

import com.vaadin.flow.component.login.*;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.*;
import com.vaadin.flow.spring.annotation.*;
import static org.apache.http.client.protocol.ClientContext.*;
import org.springframework.beans.factory.annotation.*;

import javax.annotation.*;
import java.util.*;

/**
 * Project vaadwam
 * Created by Algos
 * User: gac
 * Date: Tue, 02-Jul-2019
 * Time: 07:27
 */
@Route(value = "login")
public class Login extends VerticalLayout implements BeforeEnterObserver {

    //    private Logger adminLogger;



    //--componente di Vaadin flow invocato dall'Annotation @Tag("sa-login-view")
    private LoginOverlay login = new LoginOverlay();


    public Login() {
    }


    @PostConstruct
    protected void postConstruct() {

        login.setAction(ROUTE);

        // personalizza il branding
//        login.setTitle(new WamLoginBranding());
        login.setDescription(null);

        // non mostra bottone lost password
        login.setForgotPasswordButtonVisible(false);

        // personalizza i messaggi
        LoginI18n i18n = LoginI18n.createDefault();
        LoginI18n.ErrorMessage errore = new LoginI18n.ErrorMessage();
        errore.setTitle("Riprova");
        errore.setMessage("Username o password non corretti");
        i18n.setErrorMessage(errore);
        login.setI18n(i18n);

        // apre l'overlay
        login.setOpened(true);

    }



    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // inform the user about an authentication error
        // (yes, the API for resolving query parameters is annoying...)
        if (!event.getLocation().getQueryParameters().getParameters().getOrDefault("error", Collections.emptyList()).isEmpty()) {
            login.setError(true);
        }
    }

}// end of class
