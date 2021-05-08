package com.algos.stockscanner.views.admin;

import com.algos.stockscanner.Application;
import com.algos.stockscanner.beans.Utils;
import com.algos.stockscanner.services.*;
import com.algos.stockscanner.views.main.MainView;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import javax.annotation.PostConstruct;
import java.util.Optional;

@Route(value = "admin", layout = MainView.class)
@PageTitle(Application.APP_NAME+" | Admin")
@CssImport("./views/admin/admin-view.css")
public class AdminView extends VerticalLayout {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private static final String MARKET_INDEXES = "Market Indexes";
    private static final String GENERATOR = "Generator";

    private Component marketIndexesComponent;
    private Component generatorComponent;


    private @Autowired
    Utils utils;

    @Autowired
    private ApplicationContext context;


    public AdminView(AdminService adminService) {
        addClassName("admin-view");
    }

    @PostConstruct
    private void init() {

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

        // show default page
        if(marketIndexesComponent==null){
            marketIndexesComponent=context.getBean(MarketIndexesPage.class);
            add(marketIndexesComponent);
        }

    }



    private void customizeHeader(HorizontalLayout header) {

        String bWidth="11em";

        header.getStyle().set("flex-direction", "row-reverse");

        Button button1 = new Button(MARKET_INDEXES, new Icon(VaadinIcon.LINE_BAR_CHART));
        button1.getStyle().set("margin-left", "0.5em");
        button1.getStyle().set("margin-right", "0.5em");
        button1.getStyle().set("width", bWidth);
        button1.setIconAfterText(true);
        button1.addClickListener((ComponentEventListener<ClickEvent<Button>>) buttonClickEvent -> {
            removeAll();
            if(marketIndexesComponent==null){
                marketIndexesComponent=context.getBean(MarketIndexesPage.class);
            }
            add(marketIndexesComponent);
        });


        Button button2 = new Button(GENERATOR, new Icon(VaadinIcon.COG_O));
        button2.getStyle().set("margin-left", "0.5em");
        button2.getStyle().set("margin-right", "0.5em");
        button2.getStyle().set("width", bWidth);
        button2.setIconAfterText(true);
        button2.addClickListener((ComponentEventListener<ClickEvent<Button>>) buttonClickEvent -> {

            log.trace("A TRACE Message");
            log.debug("A DEBUG Message");
            log.info("An INFO Message");
            log.warn("A WARN Message");
            log.error("An ERROR Message");

            removeAll();
            if(generatorComponent==null){
                generatorComponent=buildGeneratorComponent();
            }
            add(generatorComponent);
        });


        header.add(button2, button1);
    }





    private Component buildGeneratorComponent(){

        Button bDownloadIndexes = new Button("Test");

        VerticalLayout layout = new VerticalLayout();
        layout.add(bDownloadIndexes);

        return layout;

    }






}
