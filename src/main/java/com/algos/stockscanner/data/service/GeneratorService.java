package com.algos.stockscanner.data.service;

import com.algos.stockscanner.beans.Utils;
import com.algos.stockscanner.data.entity.Generator;
import com.algos.stockscanner.data.entity.MarketIndex;
import com.algos.stockscanner.views.generators.GeneratorModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vaadin.artur.helpers.CrudService;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class GeneratorService extends CrudService<Generator, Integer> {

    @Autowired
    private Utils utils;

    private GeneratorRepository repository;

    @Autowired
    private MarketIndexService marketIndexService;

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
     */
    public void entityToModel(Generator entity, GeneratorModel model) {
        model.setId(utils.toPrimitive(entity.getId()));
        model.setNumber(utils.toPrimitive(entity.getNumber()));

        MarketIndex index = entity.getIndex();
        String symbol;
        byte[] imageData;
        if (index != null) {
            imageData = index.getImage();
            symbol = index.getSymbol();
        } else {
            imageData = utils.getDefaultIndexIcon();
            symbol = "n.a.";
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
        model.setSpans(utils.toPrimitive(entity.getSpans()));

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

    /**
     * Copy data from Model to Entity
     */
    public void modelToEntity(GeneratorModel model, Generator entity){

        String symbol = model.getSymbol();
        MarketIndex index=null;
        try {
            index = marketIndexService.findUniqueBySymbol(symbol);
        } catch (Exception e) {
            e.printStackTrace();
        }
        entity.setIndex(index);

        entity.setStartDate(model.getStartDate());
        entity.setAmount(model.getAmount());
        entity.setLeverage(model.getLeverage());
        entity.setStopLoss(model.getStopLoss());
        entity.setTakeProfit(model.getTakeProfit());
        entity.setFixedDays(model.isDurationFixed());
        entity.setDays(model.getDays());
        entity.setSpans(model.getSpans());

        entity.setAmplitude(model.getAmplitude());
        entity.setAmplitudeMax(model.getAmplitudeMax());
        entity.setAmplitudeMin(model.getAmplitudeMin());
        entity.setAmplitudeSteps(model.getAmplitudeSteps());
        entity.setAmplitudePermutate(model.isPermutateAmpitude());

        entity.setAvgDays(model.getDaysLookback());
        entity.setAvgDaysMax(model.getDaysLookbackMax());
        entity.setAvgDaysMin(model.getDaysLookbackMin());
        entity.setAvgDaysSteps(model.getDaysLookbackSteps());
        entity.setAvgDaysPermutate(model.isPermutateDaysLookback());

    }



    /**
     * Standard initialization of a new entity for the database.
     * <p>
     * Initialize with default values
     */
    public void initEntity(Generator entity) {
        entity.setCreated(LocalDateTime.now());
        entity.setModified(LocalDateTime.now());
        entity.setFixedDays(true);
        entity.setLeverage(1);
        entity.setNumber(calcNextNumber());
        entity.setSpans(1);
    }


    /**
     * Standard initialization of a new model for the dialog.
     * <p>
     * Initialize with default values
     */
    public void initModel(GeneratorModel model) {
        model.setDurationFixed(true);
        model.setLeverage(1);
        model.setDaysLookback(10);
        model.setDays(30);
        model.setSpans(1);
        model.setAmplitude(10);
        model.setAmount(1000);
    }


    private int calcNextNumber() {
        int next = 1;
        Generator lastByNumber = getRepository().findFirstByOrderByNumberDesc();
        if (lastByNumber != null) {
            int number = utils.toPrimitive(lastByNumber.getNumber());
            next = number + 1;
        }
        return next;
    }

    public List<Generator> findAll() {
        return repository.findAll();
    }
}
