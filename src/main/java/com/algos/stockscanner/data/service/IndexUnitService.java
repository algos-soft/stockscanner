package com.algos.stockscanner.data.service;

import com.algos.stockscanner.beans.Utils;
import com.algos.stockscanner.data.entity.Generator;
import com.algos.stockscanner.data.entity.IndexUnit;
import com.algos.stockscanner.data.entity.MarketIndex;
import com.algos.stockscanner.data.entity.Simulation;
import com.algos.stockscanner.views.simulations.SimulationModel;
import com.vaadin.flow.data.provider.QuerySortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import org.vaadin.artur.helpers.CrudService;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class IndexUnitService extends CrudService<IndexUnit, Integer> {

    private IndexUnitRepository repository;

    @Autowired
    private Utils utils;


    public IndexUnitService(@Autowired IndexUnitRepository repository) {
        this.repository = repository;
    }


    public List<IndexUnit> fetch(int offset, int limit, Example<IndexUnit> example, List<QuerySortOrder> orders) {

        Sort sort = utils.buildSort(orders);

        Pageable pageable = new OffsetBasedPageRequest(offset, limit, sort);

        Page<IndexUnit> page;
        if (example != null) {
            page = repository.findAll(example, pageable);
        } else {
            page = repository.findAll(pageable);
        }

        return page.toList();
    }


    @Transactional
    public long deleteByIndex(MarketIndex index) {
        return repository.deleteByIndex(index);
    }


    public List<IndexUnit> findAllByIndexWithDateTimeEqualOrAfter(MarketIndex index, LocalDateTime dateTime) {
        return repository.findAllByIndexWithDateTimeEqualOrAfter(index, dateTime);
    }

    public int findFirstIdOf(MarketIndex index, LocalDateTime dateTime) {
        int id = -1;
        List<IndexUnit> indexes = repository.findAllByIndexWithDateTimeEqualOrAfter(index, dateTime, PageRequest.of(0, 1));
        if (indexes.size() == 1) {
            id = indexes.get(0).getId();
        }
        return id;
    }

    public int findFirstIdOf(MarketIndex index, LocalDate date) {
        LocalDateTime localDateTime = date.atStartOfDay();
        return findFirstIdOf(index, localDateTime);
    }

    public List<IndexUnit> findAllByIndexFromId(MarketIndex index, int id, int limit){
        PageRequest pageRequest = PageRequest.of(0, limit);
        List<IndexUnit> indexes = repository.findAllByIndexFromId(index, id, pageRequest);
        return indexes;
    }

    public int countBy(MarketIndex index) {
        IndexUnit entity = new IndexUnit();
        MarketIndex eIndex = new MarketIndex();
        eIndex.setId(index.getId());
        entity.setIndex(eIndex);
        int count = (int) repository.count(Example.of(entity));
        return count;
    }


    @Override
    protected IndexUnitRepository getRepository() {
        return repository;
    }

}
