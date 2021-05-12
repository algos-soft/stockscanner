package com.algos.stockscanner.data.service;

import com.algos.stockscanner.beans.Utils;
import com.algos.stockscanner.data.entity.Generator;
import com.algos.stockscanner.data.entity.MarketIndex;
import com.algos.stockscanner.data.entity.Simulation;
import com.algos.stockscanner.views.generators.GeneratorModel;
import com.algos.stockscanner.views.indexes.IndexModel;
import com.algos.stockscanner.views.simulations.SimulationModel;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.data.provider.QuerySortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.vaadin.artur.helpers.CrudService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class GeneratorService extends CrudService<Generator, Integer> {

    private static final Logger log = LoggerFactory.getLogger(GeneratorService.class);

    @Autowired
    private Utils utils;

    private GeneratorRepository repository;

    @Autowired
    private MarketIndexService marketIndexService;

    @Autowired
    private SimulationService simulationService;

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



    public List<GeneratorModel> fetch(int offset, int limit, Example<Generator> example, List<QuerySortOrder> orders) {

        Sort sort=utils.buildSort(orders);

        Pageable pageable = new OffsetBasedPageRequest(offset, limit, sort);

        Page<Generator> page;
        if(example!=null){
            page = repository.findAll(example, pageable);
        }else{
            page = repository.findAll(pageable);
        }

        List<GeneratorModel> list = new ArrayList<>();
        for(Generator entity : page.toList()){
            GeneratorModel model = new GeneratorModel();
            entityToModel(entity, model);
            list.add(model);
        }

        return list;
    }


    public int count(Example<Generator> example) {
        return (int)repository.count(example);
    }

    public int count() {
        return (int)repository.count();
    }

    /**
     * Copy data from Entity to Model
     */
    public void entityToModel(Generator entity, GeneratorModel model) {
        model.setId(utils.toPrimitive(entity.getId()));
        model.setNumber(utils.toPrimitive(entity.getNumber()));
        model.setName(entity.getName());
        MarketIndex index = entity.getIndex();
        String symbol=null;
        Image img;
        if (index != null) {
            byte[] imageData = index.getImage();
            img=utils.byteArrayToImage(imageData);
            symbol = index.getSymbol();
        } else {
            img = utils.getDefaultIndexIcon();
        }
        model.setSymbol(symbol);
        model.setImage(img);

        List<IndexModel> mIndexes = new ArrayList<>();
        for(MarketIndex eIndex : entity.getIndexes()){
            IndexModel mIndex = new IndexModel();
            marketIndexService.entityToModel(eIndex, mIndex);
            mIndexes.add(mIndex);
        }
        model.setIndexes(mIndexes);

        List<SimulationModel> mSimulations = new ArrayList<>();
        for(Simulation eSimulation : entity.getSimulations()){
            SimulationModel mSimulation = new SimulationModel();
            simulationService.entityToModel(eSimulation, mSimulation);
            mSimulations.add(mSimulation);
        }
        model.setSimulations(mSimulations);

        model.setStartDate(entity.getStartDateLD());
        model.setAmount(utils.toPrimitive(entity.getAmount()));
        model.setStopLoss(utils.toPrimitive(entity.getStopLoss()));
        model.setTakeProfit(utils.toPrimitive(entity.getTakeProfit()));
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

        model.setPermutateIndexes(utils.toPrimitive(entity.getIndexesPermutate()));

    }

    /**
     * Copy data from Model to Entity.
     * (Copy only the data that can be changed by the dialog)
     */
    public void modelToEntity(GeneratorModel model, Generator entity){

        entity.setName(model.getName());

        String symbol = model.getSymbol();
        MarketIndex index=null;
        if(symbol!=null){
            try {
                index = marketIndexService.findUniqueBySymbol(symbol);
            } catch (Exception e) {
                log.error("can't find unique record for symbol "+symbol, e);
            }
        }
        entity.setIndex(index);

        entity.getIndexes().clear();
        for(IndexModel iModel : model.getIndexes()){
            MarketIndex iEntity = marketIndexService.get(iModel.getId()).get();
            entity.getIndexes().add(iEntity);
        }

//        entity.getSimulations().clear();
//        for(SimulationModel iModel : model.getSimulations()){
//            Simulation iEntity = simulationService.get(iModel.getId()).get();
//            entity.getSimulations().add(iEntity);
//        }

        entity.setStartDateLD(model.getStartDate());
        entity.setAmount(model.getAmount());
        entity.setStopLoss(model.getStopLoss());
        entity.setTakeProfit(model.getTakeProfit());
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

        entity.setIndexesPermutate(model.isPermutateIndexes());

    }



    /**
     * Standard initialization of a new entity for the database.
     * <p>
     * Initialize with default values
     */
    public void initEntity(Generator entity) {
        entity.setCreated(LocalDateTime.now());
        entity.setModified(LocalDateTime.now());
        entity.setNumber(calcNextNumber());
        entity.setSpans(1);
    }


    /**
     * Standard initialization of a new model for the dialog.
     * <p>
     * Initialize with default values
     */
    public void initModel(GeneratorModel model) {
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
