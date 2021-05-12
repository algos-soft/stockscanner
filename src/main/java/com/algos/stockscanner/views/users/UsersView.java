package com.algos.stockscanner.views.users;

import com.algos.stockscanner.*;
import com.algos.stockscanner.beans.*;
import com.algos.stockscanner.data.entity.*;
import com.algos.stockscanner.data.service.*;
import com.algos.stockscanner.views.*;
import com.algos.stockscanner.views.main.*;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.*;
import com.vaadin.flow.component.grid.*;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.data.provider.*;
import com.vaadin.flow.router.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.*;
import org.springframework.data.domain.*;

import javax.annotation.*;
import java.util.*;

/**
 * Project stockscanner
 * Created by Algos
 * User: gac
 * Date: mar, 11-mag-2021
 * Time: 09:36
 */
@Route(value = "users", layout = MainView.class)
@PageTitle(Application.APP_NAME + " | Users")
@PageSubtitle("Users")
//@CssImport(value = "./views/simulations/simulations-view.css")
//@CssImport(value = "./views/simulations/simulations-grid.css", themeFor = "vaadin-grid")
public class UsersView extends Div implements HasUrlParameter<String>, AfterNavigationObserver {

    @Autowired
    private UserService userService;

    @Autowired
    private Utils utils;

    private Grid<UserModel> grid;

    private Example<User> filter; // current filter

    private List<QuerySortOrder> order; // current order

    @Autowired
    ApplicationContext context;

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
    }

    @PostConstruct
    private void init() {

        addClassName("simulations-view");

        createGrid();

        VerticalLayout layout = new VerticalLayout();
        layout.getStyle().set("height", "100%");
        layout.add(grid);

        add(layout);


        // customize the header
        addAttachListener((ComponentEventListener<AttachEvent>) attachEvent -> {
            Optional<Component> parent = getParent();
            if (parent.isPresent()) {
                Optional<HorizontalLayout> customArea = utils.findCustomArea(parent.get());
                if (customArea.isPresent()) {
                    customArea.get().removeAll();
                    customizeHeader(customArea.get());
                }
            }
        });
    }

    private void customizeHeader(HorizontalLayout header) {

        header.getStyle().set("flex-direction", "row-reverse");

        Button addButton = new Button("New User", new Icon(VaadinIcon.PLUS_CIRCLE));
        addButton.getStyle().set("margin-left", "1em");
        addButton.getStyle().set("margin-right", "1em");
        addButton.setIconAfterText(true);
        addButton.addClickListener((ComponentEventListener<ClickEvent<Button>>) buttonClickEvent -> { addNewItem(); });

        header.add(addButton);
    }

    private void createGrid() {

        CallbackDataProvider<UserModel, Void> provider;
        provider = DataProvider.fromCallbacks(fetchCallback -> {
            int offset = fetchCallback.getOffset();
            int limit = fetchCallback.getLimit();
            order = fetchCallback.getSortOrders();
            return userService.fetch(offset, limit, filter, order).stream();
        }, countCallback -> {
            return userService.count();
        });

        grid = new Grid<UserModel>();

        grid.setDataProvider(provider);
        grid.setColumnReorderingAllowed(true);

        Grid.Column<UserModel> col;

        // UserName
        col = grid.addColumn(UserModel::getUserName);
        col.setHeader("UserName");
        col.setWidth("5em");
        col.setResizable(true);

        // mail
        col = grid.addColumn(UserModel::getEmail);
        col.setHeader("Mail");
        col.setWidth("10em");
        col.setResizable(true);

        // nome
        col = grid.addColumn(UserModel::getFirstName);
        col.setHeader("Nome");
        col.setWidth("5em");
        col.setResizable(true);

        // cognome
        col = grid.addColumn(UserModel::getLastName);
        col.setHeader("Cognome");
        col.setWidth("5em");
        col.setResizable(true);

        grid.addItemDoubleClickListener(listener -> openItem(listener));
    }


    /**
     * Present an empty dialog to create a new item
     */
    private void addNewItem() {

        UserDialogConfirmListener listener = new UserDialogConfirmListener() {
            @Override
            public void onConfirm(UserModel model) {
                User entity = new User();
                userService.initEntity(entity);
                userService.modelToEntity(model, entity);
                userService.update(entity);
                refreshGrid();
            }
        };

        UserModel model = new UserModel();
        userService.initModel(model);  // set defaults
        UserDialog dialog = context.getBean(UserDialog.class, model, listener);

        dialog.open();
    }

    /**
     * Present a dialog to update an item
     */
    private void openItem(ItemDoubleClickEvent<UserModel> evento) {
        UserModel model = (UserModel)evento.getItem();

        UserDialogConfirmListener listener = new UserDialogConfirmListener() {
            @Override
            public void onConfirm(UserModel model) {
                User entity=  userService.getOne(model.getId());
                userService.initEntity(entity);
                userService.modelToEntity(model, entity);
                userService.update(entity);
                refreshGrid();
            }
        };

        context.getBean(UserDialog.class, model, listener).open();
    }

    private void refreshGrid() {
        grid.select(null);
        grid.getDataProvider().refreshAll();
    }


    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        // filter by generator passed as param
    }

}
