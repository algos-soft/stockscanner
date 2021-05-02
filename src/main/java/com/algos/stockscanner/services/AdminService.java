package com.algos.stockscanner.services;

import com.algos.stockscanner.beans.Utils;
import com.algos.stockscanner.data.entity.MarketIndex;
import com.algos.stockscanner.data.enums.FrequencyTypes;
import com.algos.stockscanner.data.enums.IndexCategories;
import com.algos.stockscanner.runner.GeneratorRunner;
import com.algos.stockscanner.utils.Du;
import com.algos.stockscanner.views.indexes.IndexModel;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.server.Command;
import org.claspina.confirmdialog.ButtonOption;
import org.claspina.confirmdialog.ConfirmDialog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.vaadin.artur.helpers.CrudService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
public class AdminService {

    @Autowired
    private Utils utils;

    @Autowired
    private ApplicationContext context;

    public AdminService() {
    }

    /**
     * Download index data for a given list of indexes.
     * <p></p>
     * Each index is managed sequentially.
     * A new thread is created for each operation and we wait for it to complete before starting the next one.
     * (the web service has a max request per seconds limitation)
     *
     * We use a thread pool so that in the future we can parallelize.
     */
    public void downloadIndexData(List<MarketIndex> indexes){


        for(MarketIndex index : indexes){


        }
    }


    /**
     * Download index data for an indexes in a separate thread.
     * <p></p>
     * @param index the MarketIndex
     * @param mode the update mode:
     * ALL - delete all index data and load all the available data in the db
     * DATE - add/update all data starting from the given date included
     * @param startDate in case of DATE mode, the date where to begin the update od the data in the db,
     */
    public void downloadIndexData(MarketIndex index, String mode, LocalDate startDate){
        DownloadIndexDataCallable callable = context.getBean(DownloadIndexDataCallable.class);
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        Future<DownloadIndexDataStatus> result = executorService.submit(callable);

//            result.get();
//            result.cancel()

    }


}
