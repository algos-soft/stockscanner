package com.algos.stockscanner.views.main;

import java.util.Optional;

import com.algos.stockscanner.views.simulations.SimulationsView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.tabs.TabsVariant;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.router.PageTitle;
import com.algos.stockscanner.views.generators.GeneratorsView;
import com.algos.stockscanner.views.indexes.IndexesView;
import com.algos.stockscanner.views.settings.SettingsView;
import com.algos.stockscanner.views.about.AboutView;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.CssImport;

/**
 * The main view is a top-level placeholder for other views.
 */
@PWA(name = "StockScanner", shortName = "StockScanner", enableInstallPrompt = false)
@JsModule("./styles/shared-styles.js")
@CssImport("./views/main/main-view.css")
@Push   // or you can not access the UI from non-UI threads. This annotation must be on a top-level layout.
public class MainView extends AppLayout {

    private final Tabs menu;
    private H1 viewTitle;

    public MainView() {
        setPrimarySection(Section.DRAWER);
        addToNavbar(true, createHeaderContent());
        menu = createMenu();
        addToDrawer(createDrawerContent(menu));
    }

    private Component createHeaderContent() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setId("header");
        layout.getThemeList().set("dark", true);
        layout.setWidthFull();
        layout.setSpacing(false);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        viewTitle = new H1();

        HorizontalLayout customArea = new HorizontalLayout();
        customArea.setId("custom");
        customArea.setWidthFull();

        customArea.add(new Label("custom1"));
        customArea.add(new Label("pluto"));
        customArea.add(new Label("paperino"));
        customArea.getStyle().set("margin-left","1em");
        customArea.getStyle().set("margin-right","1em");

        layout.add(new DrawerToggle());
        layout.add(viewTitle);
        layout.add(customArea);
        layout.add(new Avatar());
        return layout;
    }

    private Component createDrawerContent(Tabs menu) {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.getThemeList().set("spacing-s", true);
        layout.setAlignItems(FlexComponent.Alignment.STRETCH);
        HorizontalLayout logoLayout = new HorizontalLayout();
        logoLayout.setId("logo");
        logoLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        logoLayout.add(new Image("images/logo.png", "StockScanner logo"));
        logoLayout.add(new H1("StockScanner"));
        layout.add(logoLayout, menu);
        return layout;
    }

    private Tabs createMenu() {
        final Tabs tabs = new Tabs();
        tabs.setOrientation(Tabs.Orientation.VERTICAL);
        tabs.addThemeVariants(TabsVariant.LUMO_MINIMAL);
        tabs.setId("tabs");
        tabs.add(createMenuItems());
        return tabs;
    }

    private Component[] createMenuItems() {
        return new Tab[]{
                createTab("Generators", GeneratorsView.class),
                createTab("Simulations", SimulationsView.class),
                createTab("Indexes", IndexesView.class),
                createTab("Settings", SettingsView.class),
                createTab("About", AboutView.class)};
    }

    private static Tab createTab(String text, Class<? extends Component> navigationTarget) {
        final Tab tab = new Tab();
        tab.add(new RouterLink(text, navigationTarget));
        ComponentUtil.setData(tab, Class.class, navigationTarget);
        return tab;
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        getTabForComponent(getContent()).ifPresent(menu::setSelectedTab);
        viewTitle.setText(getCurrentPageTitle());
    }

    private Optional<Tab> getTabForComponent(Component component) {
        Optional<Tab> foundTab = menu.getChildren().filter(tab -> ComponentUtil.getData(tab, Class.class).equals(component.getClass()))
                .findFirst().map(Tab.class::cast);

        return foundTab;

    }

    private String getCurrentPageTitle() {
        PageTitle title = getContent().getClass().getAnnotation(PageTitle.class);
        return title == null ? "" : title.value();
    }
}
