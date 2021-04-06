package com.algos.stockscanner.data.service;

import com.algos.stockscanner.data.entity.MarketIndex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vaadin.artur.helpers.CrudService;

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


    @Override
    protected MarketIndexRepository getRepository() {
        return repository;
    }

}
