package com.algos.stockscanner.views.users;

import com.vaadin.flow.component.dialog.*;
import com.vaadin.flow.spring.annotation.SpringComponent;
import org.springframework.context.annotation.Scope;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;

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

}
