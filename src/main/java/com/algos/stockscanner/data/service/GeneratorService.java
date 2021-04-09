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

        model.setStartDate(entity.getStartDate());
        model.setAmount(utils.toPrimitive(entity.getAmount()));
        model.setLeverage(utils.toPrimitive(entity.getLeverage()));
        model.setStopLoss(utils.toPrimitive(entity.getStopLoss()));
        model.setTakeProfit(utils.toPrimitive(entity.getTakeProfit()));
        model.setDurationFixed(utils.toPrimitive(entity.getFixedDays()));
        model.setDays(utils.toPrimitive(entity.getDays()));

        model.setAmplitude(utils.toPrimitive(entity.getAmplitude()));
        model.setAmplitudeMin(utils.toPrimitive(entity.getAmplitudeMin()));
        model.setAmplitudeMax(utils.toPrimitive(entity.getAmplitudeMax()));
        model.setAmplitudeSteps(utils.toPrimitive(entity.getAmplitudeSteps()));
        model.setPermutateAmpitude(utils.toPrimitive(entity.getAmplitudePermutate()));

        model.setDaysLookback(utils.toPrimitive(entity.getAvgDays()));
        model.setDaysLookbackMin(utils.toPrimitive(entity.getAvgDaysMin()));
        model.setDaysLookbackMax(utils.toPrimitive(entity.getAvgDaysMax()));
        model.setDaysLookbackSteps(utils.toPrimitive(entity.getAvgDaysSteps()));
        model.setPermutateDaysLookback(utils.toPrimitive(entity.getAvgDaysPermutate()));


    }

}
