package com.algos.stockscanner.data.service;

import com.algos.stockscanner.data.entity.IndexUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vaadin.artur.helpers.CrudService;

@Service
public class IndexUnitService extends CrudService<IndexUnit, Integer> {

    private IndexUnitRepository repository;

    public IndexUnitService(@Autowired IndexUnitRepository repository) {
        this.repository = repository;
    }

    @Override
    protected IndexUnitRepository getRepository() {
        return repository;
    }

}
