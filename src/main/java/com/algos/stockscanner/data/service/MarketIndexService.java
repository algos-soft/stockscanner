package com.algos.stockscanner.data.service;

import com.algos.stockscanner.data.entity.MarketIndex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vaadin.artur.helpers.CrudService;

@Service
public class MarketIndexService extends CrudService<MarketIndex, Integer> {

    private MarketIndexRepository repository;

    public MarketIndexService(@Autowired MarketIndexRepository repository) {
        this.repository = repository;
    }

    @Override
    protected MarketIndexRepository getRepository() {
        return repository;
    }

}
