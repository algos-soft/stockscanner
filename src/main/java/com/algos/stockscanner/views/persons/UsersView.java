package com.algos.stockscanner.views.persons;

import com.algos.stockscanner.*;
import com.algos.stockscanner.data.entity.*;
import com.algos.stockscanner.data.service.*;
import com.algos.stockscanner.views.*;
import com.algos.stockscanner.views.main.*;
import com.vaadin.flow.component.grid.*;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.data.provider.*;
import com.vaadin.flow.router.*;
import org.springframework.beans.factory.annotation.*;
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
    private UserService personService;

    private Grid<UserModel> grid;
    private Example<User> filter; // current filter
    private List<QuerySortOrder> order; // current order


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
    }

    private void createGrid() {

        CallbackDataProvider<UserModel,Void> provider;
        provider = DataProvider.fromCallbacks(fetchCallback -> {
            int offset = fetchCallback.getOffset();
            int limit = fetchCallback.getLimit();
            order = fetchCallback.getSortOrders();
            return personService.fetch(offset, limit, filter, order).stream();
        }, countCallback -> {
            return personService.count();
        });

        grid = new Grid<UserModel>();

        grid.setDataProvider(provider);
        grid.setColumnReorderingAllowed(true);

        Grid.Column<UserModel> col;
    }


    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        // filter by generator passed as param
    }

}
