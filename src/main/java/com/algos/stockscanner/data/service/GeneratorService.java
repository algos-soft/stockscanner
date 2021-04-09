package com.algos.stockscanner.data.service;

import com.algos.stockscanner.beans.Utils;
import com.algos.stockscanner.data.entity.Generator;
import com.algos.stockscanner.data.entity.MarketIndex;
import com.algos.stockscanner.views.generators.GeneratorModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vaadin.artur.helpers.CrudService;

@Service
public class GeneratorService extends CrudService<Generator, Integer> {

    @Autowired
    private Utils utils;

    private GeneratorRepository repository;

    public GeneratorService(@Autowired GeneratorRepository repository) {
        this.repository = repository;
    }


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
    protected GeneratorRepository getRepository() {
        return repository;
    }

    /**
     * Copy data from Entity to Model
     * */
    public void entityToModel(Generator entity, GeneratorModel model){
        model.setId(utils.toPrimitive(entity.getId()));
        MarketIndex index = entity.getIndex();
        String symbol;
        byte[] imageData;
        if(index!=null){
            imageData=index.getImage();
            symbol=index.getSymbol();
        } else{
            imageData=utils.getDefaultIndexIcon();
            symbol="n.a.";
        }
        model.setSymbol(symbol);
        model.setImage(utils.byteArrayToImage(imageData));

        model.setAmount(utils.toPrimitive(entity.getAmount()));
        model.setStartDate(entity.getStartDate());
        model.setAmplitude(utils.toPrimitive(entity.getAmplitude()));


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
