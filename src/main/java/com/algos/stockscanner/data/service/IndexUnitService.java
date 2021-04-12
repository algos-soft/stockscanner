package com.algos.stockscanner.data.service;

import com.algos.stockscanner.data.entity.Generator;
import com.algos.stockscanner.data.entity.IndexUnit;
import com.algos.stockscanner.data.entity.MarketIndex;
import com.algos.stockscanner.data.entity.Simulation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.vaadin.artur.helpers.CrudService;

import javax.transaction.Transactional;

@Service
public class IndexUnitService extends CrudService<IndexUnit, Integer> {

    private IndexUnitRepository repository;

    public IndexUnitService(@Autowired IndexUnitRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public long deleteByIndex (MarketIndex index){
        return repository.deleteByIndex(index);
    }


    public int countBy(MarketIndex index) {
        IndexUnit entity = new IndexUnit();
        entity.setIndex(index);
        return (int)repository.count(Example.of(entity));
    }


    @Override
    protected IndexUnitRepository getRepository() {
        return repository;
    }

}
