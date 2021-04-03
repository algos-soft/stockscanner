package com.algos.stockscanner.views.indexes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.algos.stockscanner.data.entity.MarketIndex;
import com.algos.stockscanner.beans.Utils;
import com.algos.stockscanner.data.service.MarketIndexService;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.IronIcon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.PageTitle;
import com.algos.stockscanner.views.main.MainView;
import com.vaadin.flow.component.dependency.CssImport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import javax.annotation.PostConstruct;

@Route(value = "indexes", layout = MainView.class)
@PageTitle("Indexes")
@CssImport("./views/indexes/indexes-view.css")
public class IndexesView extends Div implements AfterNavigationObserver {

    Grid<IndexModel> grid = new Grid<>();

    @Autowired
    private  Utils utils;

    @Autowired
    private MarketIndexService marketIndexService;

    @Autowired
    private ApplicationContext context;


    public IndexesView() {
    }


    @PostConstruct
    private void init(){
        addClassName("indexes-view");
        setSizeFull();
        grid.setHeight("100%");
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_NO_ROW_BORDERS);
        grid.addComponentColumn(index -> createCard(index));
        add(grid);

        // customize the header
        addAttachListener((ComponentEventListener<AttachEvent>) attachEvent -> {
            Optional<Component> parent = getParent();
            if(parent.isPresent()){
                Optional<HorizontalLayout> customArea = utils.findCustomArea(parent.get());
                if(customArea.isPresent()){
                    customArea.get().removeAll();
                    customizeHeader(customArea.get());
                }
            }
        });
    }

    private void customizeHeader(HorizontalLayout header){
        ComboBox searchBox = new ComboBox();
        searchBox.setPlaceholder("Search index");
        searchBox.setAllowCustomValue(false);
        searchBox.addValueChangeListener(event -> {
            if (event.getValue() == null) {
                //value.setText("No option selected");
            } else {
                //value.setText("Selected: " + event.getValue());
            }
        });

        Button addButton = new Button("Add Index",  new Icon(VaadinIcon.PLUS_CIRCLE));
        addButton.getStyle().set("margin-left","1em");
        addButton.getStyle().set("margin-right","1em");
        addButton.setIconAfterText(true);
        addButton.addClickListener((ComponentEventListener<ClickEvent<Button>>) buttonClickEvent -> {
            addNewIndex();
        });

        header.add(searchBox);
        header.add(addButton);
    }


    /**
     * Present an empty dialog to create a new index
     */
    private void addNewIndex(){

        IndexDialogConfirmListener listener =  new IndexDialogConfirmListener() {
            @Override
            public void onConfirm(IndexModel model) {
                MarketIndex entity = new MarketIndex();
                updateEntity(entity, model);
                //MarketIndex entity = model.toEntity();
                marketIndexService.update(entity);
            }
        };

        IndexDialog dialog = context.getBean(IndexDialog.class, listener);

        dialog.open();
    }



    private HorizontalLayout createCard(IndexModel model) {

        HorizontalLayout card = new HorizontalLayout();
        card.addClassName("card");
        card.setSpacing(false);
        card.getThemeList().add("spacing-s");

        Image image = model.getImage();

        VerticalLayout body = new VerticalLayout();
        body.addClassName("description");
        body.setSpacing(false);
        body.setPadding(false);

        HorizontalLayout header = new HorizontalLayout();
        header.addClassName("header");
        header.setSpacing(false);
        header.getThemeList().add("spacing-s");

        Span symbol = new Span(model.getSymbol());
        symbol.addClassName("symbol");

        Span name = new Span(model.getName());
        name.addClassName("name");

        Span date = new Span(model.getDate());
        date.addClassName("date");
        header.add(symbol, name, date);

        Span post = new Span(model.getPost());
        post.addClassName("post");

        HorizontalLayout actions = new HorizontalLayout();
        actions.addClassName("actions");
        actions.setSpacing(false);
        actions.getThemeList().add("spacing-s");

        IronIcon likeIcon = new IronIcon("vaadin", "heart");
        Span likes = new Span(model.getLikes());
        likes.addClassName("likes");
        IronIcon commentIcon = new IronIcon("vaadin", "comment");
        Span comments = new Span(model.getComments());
        comments.addClassName("comments");
        IronIcon shareIcon = new IronIcon("vaadin", "connect");
        Span shares = new Span(model.getShares());
        shares.addClassName("shares");

        Component action = buildActionCombo(model);

        actions.add(likeIcon, likes, commentIcon, comments, shareIcon, shares);
        body.add(header, post, actions);

        card.add(image, body, action);
        return card;
    }


    private Component buildActionCombo(IndexModel model){

        MenuBar menuBar = new MenuBar();
        MenuItem account = menuBar.addItem("Actions...");

        account.getSubMenu().addItem("Download data", e -> System.out.println("Download data"));

        // edit item
        account.getSubMenu().addItem("Edit index", e -> {

            Optional<MarketIndex> entity = marketIndexService.get(model.getId());

            IndexDialogConfirmListener listener =  new IndexDialogConfirmListener() {
                @Override
                public void onConfirm(IndexModel model) {
                    updateEntity(entity.get(), model);
                    marketIndexService.update(entity.get());
                    grid.getDataProvider().refreshAll();
                }
            };

            //IndexModel model = IndexModel.fromEntity(entity.get());
            IndexDialog dialog = context.getBean(IndexDialog.class, model, listener);

            dialog.open();

        });

        account.getSubMenu().addItem("Delete index", e -> System.out.println("Delete index"));

        return menuBar;

    }

    /**
     * Update entity from model
     */
    private void updateEntity(MarketIndex entity, IndexModel model){
        entity.setImage(model.getImageData());
        entity.setSymbol(model.getSymbol());
        entity.setName(model.getName());
        entity.setBuySpreadPercent(model.getBuySpreadPercent());
        entity.setOvnBuyDay(model.getOvnBuyDay());
        entity.setOvnBuyWe(model.getOvnBuyWe());
        entity.setOvnSellDay(model.getOvnSellDay());
        entity.setOvnSellWe(model.getOvnSellWe());
    }



    @Override
    public void afterNavigation(AfterNavigationEvent event) {

        // Set some data when this view is displayed.
        List<IndexModel> outList=new ArrayList<>();

        Pageable p = Pageable.unpaged();
        Page<MarketIndex> page = marketIndexService.list(p);

        page.stream().forEach(e -> {
            outList.add(createIndex(e));
        });

        grid.setItems(outList);


    }


    private IndexModel createIndex(MarketIndex index) {
        IndexModel m = new IndexModel();
        m.setId(index.getId());
        m.setSymbol(index.getSymbol());
        m.setImageData(index.getImage());
        m.setImage(utils.byteArrayToImage(index.getImage()));
        m.setSymbol(index.getSymbol());
        m.setName(index.getName());
        m.setBuySpreadPercent(index.getBuySpreadPercent());
        m.setOvnBuyDay(index.getOvnBuyDay());
        m.setOvnBuyWe(index.getOvnBuyWe());
        m.setOvnSellDay(index.getOvnSellDay());
        m.setOvnSellWe(index.getOvnSellWe());
        return m;
    }


}
