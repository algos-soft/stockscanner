package com.algos.stockscanner.data.service;

import com.algos.stockscanner.beans.Utils;
import com.algos.stockscanner.data.entity.IndexUnit;
import com.algos.stockscanner.data.entity.MarketIndex;
import com.algos.stockscanner.data.entity.SimulationItem;
import com.algos.stockscanner.utils.Du;
import com.vaadin.flow.data.provider.QuerySortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.vaadin.artur.helpers.CrudService;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class SimulationItemService extends CrudService<SimulationItem, Integer> {

    private SimulationItemRepository repository;

    @Autowired
    private Utils utils;

    public SimulationItemService(@Autowired SimulationItemRepository repository) {
        this.repository = repository;
    }


    @Override
    protected SimulationItemRepository getRepository() {
        return repository;
    }

}
