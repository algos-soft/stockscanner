package com.algos.stockscanner.data.service;

import com.algos.stockscanner.beans.Utils;
import com.algos.stockscanner.data.entity.MarketIndex;
import com.algos.stockscanner.data.enums.FrequencyTypes;
import com.algos.stockscanner.data.enums.IndexCategories;
import com.algos.stockscanner.views.indexes.IndexModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.vaadin.artur.helpers.CrudService;

import java.util.List;
import java.util.Optional;

@Service
public class MarketIndexService extends CrudService<MarketIndex, Integer> {

    @Autowired
    private Utils utils;

    @Autowired
    private IndexUnitService indexUnitService;

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



    public List<MarketIndex> fetch(int offset, int limit, Example<MarketIndex> example, Sort sort) {

        // default sort - by symbol
        if(sort==null){
            sort = Sort.by("symbol");
        }

        Pageable pageable = new OffsetBasedPageRequest(offset, limit, sort);

        Page<MarketIndex> page;
        if(example!=null){
            page = repository.findAll(example, pageable);
        }else{
            page = repository.findAll(pageable);
        }

        return page.toList();
    }

    public int count(Example<MarketIndex> example) {
        return (int)repository.count(example);
    }


    public List<MarketIndex> findAll ()  {
        return repository.findAll();
    }

    public int countDataPoints(MarketIndex index){
        return indexUnitService.countBy(index);
    }


    @Override
    protected MarketIndexRepository getRepository() {
        return repository;
    }

    /**
     * Copy data from Entity to Model*/
    public void entityToModel(MarketIndex entity, IndexModel model){
        model.setId(entity.getId());
        model.setSymbol(entity.getSymbol());
        model.setName(entity.getName());

        String categoryCode=entity.getCategory();
        Optional<IndexCategories> oCategory= IndexCategories.getItem(categoryCode);
        if(oCategory.isPresent()){
            model.setCategory(oCategory.get());
        }

        model.setImageData(entity.getImage());
        model.setImage(utils.byteArrayToImage(entity.getImage()));
        model.setSymbol(entity.getSymbol());

        model.setBuySpreadPercent(utils.toPrimitive(entity.getBuySpreadPercent()));
        model.setOvnBuyDay(utils.toPrimitive(entity.getOvnBuyDay()));
        model.setOvnBuyWe(utils.toPrimitive(entity.getOvnBuyWe()));
        model.setOvnSellDay(utils.toPrimitive(entity.getOvnSellDay()));
        model.setOvnSellWe(utils.toPrimitive(entity.getOvnSellWe()));

        model.setUnitsFrom(utils.toLocalDate(entity.getUnitsFrom()));
        model.setUnitsTo(utils.toLocalDate(entity.getUnitsTo()));
        model.setNumUnits(utils.toPrimitive(entity.getNumUnits()));

        String frequencyCode=entity.getUnitFrequency();
        Optional<FrequencyTypes> oFrequency= FrequencyTypes.getItem(frequencyCode);
        if(oFrequency.isPresent()){
            model.setUnitFrequency(oFrequency.get());
        }

    }




}
