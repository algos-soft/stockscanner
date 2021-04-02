package com.algos.stockscanner.views.indexes;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.algos.stockscanner.data.entity.MarketIndex;
import com.algos.stockscanner.data.service.MarketIndexRepository;
import com.algos.stockscanner.utils.Utils;
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

@Route(value = "indexes", layout = MainView.class)
@PageTitle("Indexes")
@CssImport("./views/indexes/indexes-view.css")
public class IndexesView extends Div implements AfterNavigationObserver {

    Grid<IndexModel> grid = new Grid<>();

    private @Autowired Utils utils;
    private @Autowired  MarketIndexRepository marketIndexRepository;

    public IndexesView() {
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
        IndexDialog dialog = new IndexDialog(null, new IndexDialogConfirmListener() {
            @Override
            public void onConfirm(IndexModel model) {
                MarketIndex entity = model.toEntity();
                marketIndexRepository.save(entity);
            }
        });
        dialog.open();
    }



    private HorizontalLayout createCard(IndexModel index) {
        HorizontalLayout card = new HorizontalLayout();
        card.addClassName("card");
        card.setSpacing(false);
        card.getThemeList().add("spacing-s");

        Image image = new Image();
        image.setSrc(index.getImage());
        VerticalLayout description = new VerticalLayout();
        description.addClassName("description");
        description.setSpacing(false);
        description.setPadding(false);

        HorizontalLayout header = new HorizontalLayout();
        header.addClassName("header");
        header.setSpacing(false);
        header.getThemeList().add("spacing-s");

        Span name = new Span(index.getName());
        name.addClassName("name");
        Span date = new Span(index.getDate());
        date.addClassName("date");
        header.add(name, date);

        Span post = new Span(index.getPost());
        post.addClassName("post");

        HorizontalLayout actions = new HorizontalLayout();
        actions.addClassName("actions");
        actions.setSpacing(false);
        actions.getThemeList().add("spacing-s");

        IronIcon likeIcon = new IronIcon("vaadin", "heart");
        Span likes = new Span(index.getLikes());
        likes.addClassName("likes");
        IronIcon commentIcon = new IronIcon("vaadin", "comment");
        Span comments = new Span(index.getComments());
        comments.addClassName("comments");
        IronIcon shareIcon = new IronIcon("vaadin", "connect");
        Span shares = new Span(index.getShares());
        shares.addClassName("shares");

        Component action = buildActionCombo();

        actions.add(likeIcon, likes, commentIcon, comments, shareIcon, shares);
        description.add(header, post, actions);
        card.add(image, description, action);
        return card;
    }


    private Component buildActionCombo(){

        MenuBar menuBar = new MenuBar();
        MenuItem account = menuBar.addItem("Actions...");

        account.getSubMenu().addItem("Download data", e -> System.out.println("Download data"));
        account.getSubMenu().addItem("Edit index", e -> System.out.println("Edit index"));
        account.getSubMenu().addItem("Delete index", e -> System.out.println("Delete index"));

        return menuBar;

    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {

        // Set some data when this view is displayed.
        List<IndexModel> indexes = Arrays.asList( //
                createPerson("https://randomuser.me/api/portraits/men/42.jpg", "John Smith", "May 8",
                        "In publishing and graphic design, Lorem ipsum is a placeholder text commonly used to demonstrate the visual form of a document without relying on meaningful content (also called greeking).",
                        "1K", "500", "20"),
                createPerson("https://randomuser.me/api/portraits/women/42.jpg", "Abagail Libbie", "May 3",
                        "In publishing and graphic design, Lorem ipsum is a placeholder text commonly used to demonstrate the visual form of a document without relying on meaningful content (also called greeking).",
                        "1K", "500", "20"),
                createPerson("https://randomuser.me/api/portraits/men/24.jpg", "Alberto Raya", "May 3",

                        "In publishing and graphic design, Lorem ipsum is a placeholder text commonly used to demonstrate the visual form of a document without relying on meaningful content (also called greeking).",
                        "1K", "500", "20"),
                createPerson("https://randomuser.me/api/portraits/women/24.jpg", "Emmy Elsner", "Apr 22",

                        "In publishing and graphic design, Lorem ipsum is a placeholder text commonly used to demonstrate the visual form of a document without relying on meaningful content (also called greeking).",
                        "1K", "500", "20"),
                createPerson("https://randomuser.me/api/portraits/men/76.jpg", "Alf Huncoot", "Apr 21",

                        "In publishing and graphic design, Lorem ipsum is a placeholder text commonly used to demonstrate the visual form of a document without relying on meaningful content (also called greeking).",
                        "1K", "500", "20"),
                createPerson("https://randomuser.me/api/portraits/women/76.jpg", "Lidmila Vilensky", "Apr 17",

                        "In publishing and graphic design, Lorem ipsum is a placeholder text commonly used to demonstrate the visual form of a document without relying on meaningful content (also called greeking).",
                        "1K", "500", "20"),
                createPerson("https://randomuser.me/api/portraits/men/94.jpg", "Jarrett Cawsey", "Apr 17",
                        "In publishing and graphic design, Lorem ipsum is a placeholder text commonly used to demonstrate the visual form of a document without relying on meaningful content (also called greeking).",
                        "1K", "500", "20"),
                createPerson("https://randomuser.me/api/portraits/women/94.jpg", "Tania Perfilyeva", "Mar 8",

                        "In publishing and graphic design, Lorem ipsum is a placeholder text commonly used to demonstrate the visual form of a document without relying on meaningful content (also called greeking).",
                        "1K", "500", "20"),
                createPerson("https://randomuser.me/api/portraits/men/16.jpg", "Ivan Polo", "Mar 5",

                        "In publishing and graphic design, Lorem ipsum is a placeholder text commonly used to demonstrate the visual form of a document without relying on meaningful content (also called greeking).",
                        "1K", "500", "20"),
                createPerson("https://randomuser.me/api/portraits/women/16.jpg", "Emelda Scandroot", "Mar 5",

                        "In publishing and graphic design, Lorem ipsum is a placeholder text commonly used to demonstrate the visual form of a document without relying on meaningful content (also called greeking).",
                        "1K", "500", "20"),
                createPerson("https://randomuser.me/api/portraits/men/67.jpg", "Marcos Sá", "Mar 4",

                        "In publishing and graphic design, Lorem ipsum is a placeholder text commonly used to demonstrate the visual form of a document without relying on meaningful content (also called greeking).",
                        "1K", "500", "20"),
                createPerson("https://randomuser.me/api/portraits/women/67.jpg", "Jacqueline Asong", "Mar 2",

                        "In publishing and graphic design, Lorem ipsum is a placeholder text commonly used to demonstrate the visual form of a document without relying on meaningful content (also called greeking).",
                        "1K", "500", "20")

        );

        grid.setItems(indexes);
    }

    private static IndexModel createPerson(String image, String name, String date, String post, String likes,
                                           String comments, String shares) {
        IndexModel p = new IndexModel();
        p.setImage(image);
        p.setName(name);
        p.setDate(date);
        p.setPost(post);
        p.setLikes(likes);
        p.setComments(comments);
        p.setShares(shares);

        return p;
    }

}
