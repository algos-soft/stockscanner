package com.algos.stockscanner.views.users;

import com.vaadin.flow.spring.annotation.SpringComponent;
import org.springframework.context.annotation.Scope;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;

/**
 * Project stockscanner
 * Created by Algos
 * User: gac
 * Date: mar, 11-mag-2021
 * Time: 14:48
 */
public interface UserDialogConfirmListener {
    public void onConfirm(UserModel model);
}
