package com.algos.stockscanner.views.admin;

import com.algos.stockscanner.beans.ContextStore;
import com.algos.stockscanner.beans.Utils;
import com.algos.stockscanner.data.entity.MarketIndex;
import com.algos.stockscanner.data.service.MarketIndexService;
import com.algos.stockscanner.services.AdminService;
import com.algos.stockscanner.services.MarketService;
import com.algos.stockscanner.services.UpdateIndexDataCallable;
import com.algos.stockscanner.task.TaskHandler;
import com.algos.stockscanner.task.TaskListener;
import com.algos.stockscanner.task.TaskMonitor;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.server.Command;
import org.claspina.confirmdialog.ButtonOption;
import org.claspina.confirmdialog.ConfirmDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Component
@Scope(value = SCOPE_PROTOTYPE)
@CssImport("./views/admin/admin-view.css")
public class PricesPage extends VerticalLayout {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

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

    @PostConstruct
    private void init(){

        statusLayout = new HorizontalLayout();
        statusLayout.setSpacing(false);
        statusLayout.setPadding(false);
        statusLayout.addClassName("admin-view-statuslayout");

        // button update prices
        Button bUpdatePrices = new Button("Update prices");
        bUpdatePrices.setId("adminview-bupdateprices");
        bUpdatePrices.addClickListener(new ComponentEventListener<ClickEvent<Button>>() {
            @Override
            public void onComponentEvent(ClickEvent<Button> buttonClickEvent) {
                try {
                    log.info("Update Prices action requested");

                    List<MarketIndex> indexes = new ArrayList<>();
                    indexes.add(marketIndexService.findUniqueBySymbol("AAPL"));
                    indexes.add(marketIndexService.findUniqueBySymbol("AMZN"));
                    indexes.add(marketIndexService.findUniqueBySymbol("PYPL"));

                    int intervalSec = 60/limitField.getValue();
                    List<UpdateIndexDataCallable> callables = adminService.scheduleUpdate(indexes, intervalSec);
                    for(UpdateIndexDataCallable callable : callables){
                        attachMonitorToTask(callable);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        // request limit field
        limitField=new IntegerField("Max req per minute");
        limitField.setId("adminview-reqlimitfield");
        limitField.setValue(5);

        VerticalLayout content = new VerticalLayout();
        content.add(bUpdatePrices, limitField);
        content.setHeight("100%");

        setHeight("100%");
        add(content, statusLayout);

        // retrieve the running tasks from the context, create Task Monitors and put them in the UI
        Collection<UpdateIndexDataCallable> callables = contextStore.updateIndexCallableMap.values();
        for(UpdateIndexDataCallable callable : callables){
            attachMonitorToTask(callable);
        }

    }


    /**
     * Attach a TaskMonitor to a task and add it to the status panel
     */
    private void attachMonitorToTask(UpdateIndexDataCallable callable){

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


