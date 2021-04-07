package com.algos.stockscanner.data.service;

import com.algos.stockscanner.data.entity.MarketIndex;
import com.algos.stockscanner.data.entity.Simulation;
import com.algos.stockscanner.views.simulations.SimulationModel;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.vaadin.artur.helpers.CrudService;

import java.util.ArrayList;
import java.util.List;

@Service
public class MarketIndexService extends CrudService<MarketIndex, Integer> {

    private MarketIndexRepository repository;

    public MarketIndexService(@Autowired MarketIndexRepository repository) {
        this.repository = repository;
    }

    public List<MarketIndex> findBySymbol (String symbol){
        return repository.findBySymbol(symbol);
    }

    public MarketIndex findUniqueBySymbol (String symbol) throws Exception {
        List<MarketIndex> list = repository.findBySymbol(symbol);
        if(list.size()!=1){
            if(list.size()==0){
                throw new Exception("Symbol "+symbol+" not found in database.");
            }else{
                throw new Exception("Multiple instances ("+list.size()+") of Symbol "+symbol+" present or in database.");
            }
        }
        return list.get(0);
    }



    public List<MarketIndex> fetch(int offset, int limit) {
        Pageable pageable = new OffsetBasedPageRequest(offset, limit);
        Page<MarketIndex> page = repository.findAll(pageable);
        return page.toList();
    }

    public int count() {
        return (int)repository.count();
    }

    @Override
    protected MarketIndexRepository getRepository() {
        return repository;
    }

}
