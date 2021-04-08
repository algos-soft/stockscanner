package com.algos.stockscanner.data.service;

import com.algos.stockscanner.beans.Utils;
import com.algos.stockscanner.data.entity.MarketIndex;
import com.algos.stockscanner.data.entity.Permutation;
import com.algos.stockscanner.views.permutations.PermutationModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vaadin.artur.helpers.CrudService;

import java.util.List;

@Service
public class PermutationService extends CrudService<Permutation, Integer> {

    @Autowired
    private Utils utils;

    private PermutationRepository repository;

    public PermutationService(@Autowired PermutationRepository repository) {
        this.repository = repository;
    }

//    public List<Permutation> findBySymbol (String symbol){
//        return repository.findBySymbol(symbol);
//    }

//    public MarketIndex findUniqueBySymbol (String symbol) throws Exception {
//        List<MarketIndex> list = repository.findBySymbol(symbol);
//        if(list.size()!=1){
//            if(list.size()==0){
//                throw new Exception("Symbol "+symbol+" not found in database.");
//            }else{
//                throw new Exception("Multiple instances ("+list.size()+") of Symbol "+symbol+" present or in database.");
//            }
//        }
//        return list.get(0);
//    }


    @Override
    protected PermutationRepository getRepository() {
        return repository;
    }

    /**
     * Copy data from Entity to Model
     * */
    public void entityToModel(Permutation entity, PermutationModel model){
        model.setId(utils.toPrimitive(entity.getId()));

//        model.setSymbol(entity.getSymbol());
//        model.setName(entity.getName());
//
//        String categoryCode=entity.getCategory();
//        Optional<IndexCategories> oCategory= IndexCategories.getItem(categoryCode);
//        if(oCategory.isPresent()){
//            model.setCategory(oCategory.get());
//        }
//
//        model.setImageData(entity.getImage());
//        model.setImage(utils.byteArrayToImage(entity.getImage()));
//        model.setSymbol(entity.getSymbol());
//        model.setBuySpreadPercent(entity.getBuySpreadPercent());
//        model.setOvnBuyDay(entity.getOvnBuyDay());
//        model.setOvnBuyWe(entity.getOvnBuyWe());
//        model.setOvnSellDay(entity.getOvnSellDay());
//        model.setOvnSellWe(entity.getOvnSellWe());
//
//        model.setUnitsFrom(entity.getUnitsFrom());
//        model.setUnitsTo(entity.getUnitsTo());
//        model.setNumUnits(entity.getNumUnits());
//
//        String frequencyCode=entity.getUnitFrequency();
//        Optional<FrequencyTypes> oFrequency= FrequencyTypes.getItem(frequencyCode);
//        if(oFrequency.isPresent()){
//            model.setUnitFrequency(oFrequency.get());
//        }

    }

}
