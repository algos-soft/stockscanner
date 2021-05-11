package com.algos.stockscanner.views.users;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.*;
import com.vaadin.flow.component.dialog.*;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.textfield.*;
import org.springframework.beans.factory.config.*;
import org.springframework.context.annotation.Scope;

import javax.annotation.*;

/**
 * Project stockscanner
 * Created by Algos
 * User: gac
 * Date: mar, 11-mag-2021
 * Time: 14:51
 */
@org.springframework.stereotype.Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class UserDialog extends Dialog {

    private UserModel model;

    private UserDialogConfirmListener confirmListener;

    private TextField userFld;

    private TextField passwordFld;

    private TextField emailFld;

    private TextField nomeFld;

    private TextField cognomeFld;

    public UserDialog() {
        this((UserDialogConfirmListener) null);
    }

    public UserDialog(UserDialogConfirmListener confirmListener) {
        this((UserModel) null, confirmListener);
        this.model = model;
    }

    public UserDialog(UserModel model, UserDialogConfirmListener confirmListener) {
        this.model = model;
        this.confirmListener = confirmListener;
    }

    @PostConstruct
    private void init() {
        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);
        add(buildContent());

        if (model != null) {
            populateFromModel();
        }
    }

    private Component buildContent() {

        Div layout = new Div();
        layout.addClassName("indexes-dialog");
        Component header = buildHeader();
        Component body = buildBody();
        Component footer = buildFooter();
        layout.add(header, body, footer);

        return layout;
    }

    private Component buildHeader() {
        Div header = new Div();
        header.addClassName("header");

        return header;
    }

    private Component buildBody() {
        Div body = new Div();
        body.addClassName("body");

        userFld = new TextField();
        userFld.setLabel("userName");

        passwordFld = new TextField();
        passwordFld.setLabel("password");

        emailFld = new TextField();
        emailFld.setLabel("eMail");

        nomeFld = new TextField();
        nomeFld.setLabel("Name");

        cognomeFld = new TextField();
        cognomeFld.setLabel("Cognome");

        body.add(userFld, passwordFld, emailFld, nomeFld, cognomeFld);
        return body;
    }

    private Component buildFooter() {
        Div btnLayout = new Div();
        btnLayout.addClassName("footer");

        Button confirmButton = new Button("Confirm", event -> {
            UserModel model = modelFromDialog();
            confirmListener.onConfirm(model);
            close();
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Cancel", event -> {
            close();
        });

        btnLayout.add(cancelButton, confirmButton);

        return btnLayout;
    }

    private void populateFromModel() {
        userFld.setValue(model.getUserName());
        passwordFld.setValue(model.getPassword());
        emailFld.setValue(model.getEmail());
        nomeFld.setValue(model.getFirstName());
        cognomeFld.setValue(model.getLastName());
    }

    /**
     * Build a new model or update the current model from the data displayed in the dialog
     */
    private UserModel modelFromDialog() {
        UserModel model;
        if (this.model != null) {
            model = this.model;
        }
        else {
            model = new UserModel();
        }

        model.setUserName(userFld.getValue());
        model.setPassword(passwordFld.getValue());
        model.setEmail(emailFld.getValue());
        model.setFirstName(nomeFld.getValue());
        model.setLastName(cognomeFld.getValue());

        return model;
    }

}
