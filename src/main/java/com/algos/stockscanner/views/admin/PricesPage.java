package com.algos.stockscanner.views.admin;

import com.algos.stockscanner.beans.ContextStore;
import com.algos.stockscanner.beans.Utils;
import com.algos.stockscanner.data.entity.MarketIndex;
import com.algos.stockscanner.data.service.MarketIndexService;
import com.algos.stockscanner.enums.PriceUpdateModes;
import com.algos.stockscanner.services.AdminService;
import com.algos.stockscanner.services.MarketService;
import com.algos.stockscanner.services.UpdatePricesCallable;
import com.algos.stockscanner.task.TaskHandler;
import com.algos.stockscanner.task.TaskListener;
import com.algos.stockscanner.task.TaskMonitor;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.RadioGroupVariant;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.server.Command;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.claspina.confirmdialog.ConfirmDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Component
@Scope(value = SCOPE_PROTOTYPE)
@CssImport("./views/admin/admin-view.css")
public class PricesPage extends VerticalLayout {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Value("${alphavantage.max.requests.per.minute:5}")
    private int MAX_REQ_PER_MINUTE;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private MarketService marketService;

    @Autowired
    private MarketIndexService marketIndexService;

    @Autowired
    private AdminService adminService;

    private @Autowired
    Utils utils;

    @Autowired
    private ContextStore contextStore;

    private HorizontalLayout statusLayout;

    private IntegerField limitField;

    private TextField filterFrom;
    private TextField filterTo;


    private Span filterResult;

    private RadioButtonGroup<PriceUpdateModes> optionsGroup;


    @PostConstruct
    private void init(){

        statusLayout = new HorizontalLayout();
        statusLayout.setSpacing(false);
        statusLayout.setPadding(false);
        statusLayout.addClassName("admin-view-statuslayout");

        optionsGroup = new RadioButtonGroup<>();
        optionsGroup.setLabel("Download mode");
        optionsGroup.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        optionsGroup.setItems(PriceUpdateModes.values());
        optionsGroup.setValue(PriceUpdateModes.ADD_MISSING_DATA_ONLY);
        optionsGroup.addValueChangeListener(new HasValue.ValueChangeListener<AbstractField.ComponentValueChangeEvent<RadioButtonGroup<PriceUpdateModes>, PriceUpdateModes>>() {
            @Override
            public void valueChanged(AbstractField.ComponentValueChangeEvent<RadioButtonGroup<PriceUpdateModes>, PriceUpdateModes> event) {
                updateFilter();
            }
        });


        // button update prices
        Button bUpdatePrices = new Button("Start update");
        bUpdatePrices.setId("adminview-bupdateprices");
        bUpdatePrices.addClickListener(new ComponentEventListener<ClickEvent<Button>>() {
            @Override
            public void onComponentEvent(ClickEvent<Button> buttonClickEvent) {
                try {

                    if(contextStore.updateIndexCallableMap.size()>0){
                        ConfirmDialog.createWarning().withMessage("Operation already in progress").open();
                        return;
                    }

                    List<String> symbols=buildFilteredSymbolList();

                    log.info("price update requested - "+symbols.size()+" indexes to update");
                    UpdatePricesCallable callable = adminService.scheduleUpdate(symbols, optionsGroup.getValue(), limitField.getValue());
                    attachMonitorToTask(callable);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        filterFrom=new TextField("from");
        filterFrom.setWidth("5em");
        filterFrom.addValueChangeListener((HasValue.ValueChangeListener<AbstractField.ComponentValueChangeEvent<TextField, String>>) event -> {
            updateFilter();
        });

        filterTo=new TextField("to");
        filterTo.setWidth("5em");
        filterTo.addValueChangeListener((HasValue.ValueChangeListener<AbstractField.ComponentValueChangeEvent<TextField, String>>) event -> {
            updateFilter();
        });



        filterResult = new Span();
        filterResult.setId("adminview-filterresult");

        Button bShow = new Button("show");
        bShow.addClickListener(new ComponentEventListener<ClickEvent<Button>>() {
            @Override
            public void onComponentEvent(ClickEvent<Button> buttonClickEvent) {
                List<String> symbolList=buildFilteredSymbolList();
                String csvList = String.join(", ", symbolList);
                ConfirmDialog.createInfo().withMessage(csvList).open();
            }
        });


        // request limit field
        limitField=new IntegerField("Max req per minute");
        limitField.setId("adminview-reqlimitfield");
        limitField.setValue(MAX_REQ_PER_MINUTE);


        HorizontalLayout filterRow = new HorizontalLayout();
        filterRow.setAlignItems(Alignment.BASELINE);
        filterRow.add(filterFrom, filterTo, filterResult, bShow);

        HorizontalLayout buttonRow = new HorizontalLayout();
        buttonRow.setAlignItems(Alignment.BASELINE);
        buttonRow.add(limitField, bUpdatePrices);

        Span headline=new Span("Download historic price data");
        headline.getStyle().set("font-weight","bold");

        VerticalLayout content = new VerticalLayout();
        content.add(headline, optionsGroup, filterRow, buttonRow);
        content.setHeight("100%");

        setHeight("100%");
        add(content, statusLayout);

        updateFilter();

        // retrieve the running tasks from the context, create Task Monitors and put them in the UI
        Collection<UpdatePricesCallable> callables = contextStore.updateIndexCallableMap.values();
        for(UpdatePricesCallable callable : callables){
            attachMonitorToTask(callable);
        }

    }


    private void updateFilter(){
        List<String> symbolList=buildFilteredSymbolList();
        String html="selected symbols: <strong>"+symbolList.size()+"</strong>";
        filterResult.getElement().setProperty("innerHTML", html);
    }

//    /**
//     * Build the list of symbols based on the current filters
//     */
//    private List<String> buildFilteredSymbolList(){
//        List<String> list=new ArrayList<>();
//        List<MarketIndex> entities = marketIndexService.findAll();
//        for(MarketIndex index : entities){
//            list.add(index.getSymbol());
//        }
//        list= filterFromSelect(list);
//        list= filterToSelect(list);
//        Collections.sort(list);
//        return list;
//    }

    /**
     * Build the list of symbols based on the current filters
     */
    private List<String> buildFilteredSymbolList(){
        List<String> list=new ArrayList<>();
        List<MarketIndex> entities = marketIndexService.findAll();
        for(MarketIndex index : entities){
            list.add(index.getSymbol());
        }
        list= filterFrom(list);
        list= filterTo(list);
        Collections.sort(list);
        return list;
    }

    private List<String> filterFrom(List<String> list){
        List<String> filteredList=new ArrayList<>();
        String from = filterFrom.getValue();
        if(StringUtils.isEmpty(from)){
            return list;
        }else{
            for(String string : list){
                if(string.toUpperCase().compareTo(from.toUpperCase())>=0){
                    filteredList.add(string);
                }
            }
        }
        return filteredList;
    }

    private List<String> filterTo(List<String> list){
        List<String> filteredList=new ArrayList<>();
        String to = filterTo.getValue();
        if(StringUtils.isEmpty(to)){
            return list;
        }else{
            for(String string : list){
                if(string.toUpperCase().compareTo(to.toUpperCase())<=0){
                    filteredList.add(string);
                }
            }
        }
        return filteredList;
    }


    /**
     * Attach a TaskMonitor to a task and add it to the status panel
     */
    private void attachMonitorToTask(UpdatePricesCallable callable){

        UI ui = UI.getCurrent();

        // obtain a handle to interrupt/manage the task
        TaskHandler handler=callable.obtainHandler();

        // GUI component handling events coming from the monitor
        TaskMonitor taskMonitor = context.getBean(TaskMonitor.class);
        TaskMonitor.MonitorListener listener = new TaskMonitor.MonitorListener() {

            // warning, the listener is called on another thread

            @Override
            public void onAborted() {
                handler.abort();
            }

            @Override
            public void onClosed() {
                ui.access((Command) () -> statusLayout.remove(taskMonitor));
            }
        };
        taskMonitor.setMonitorListener(listener);
        //taskMonitor.setAutoClose(true);


        // listen to events happening in the Callable
        callable.addListener(new TaskListener() {

            // warning, the listener is called on another thread

            @Override
            public void onStarted(Object info) {
            }

            @Override
            public void onProgress(int current, int total, Object progressInfo) {
                taskMonitor.onProgress(current, total, progressInfo);
            }

            @Override
            public void onCompleted(Object completionInfo) {
                taskMonitor.onCompleted(completionInfo);
            }

            @Override
            public void onError(Exception e) {
                taskMonitor.onError(e);
            }
        });

        statusLayout.add(taskMonitor);

    }



}


