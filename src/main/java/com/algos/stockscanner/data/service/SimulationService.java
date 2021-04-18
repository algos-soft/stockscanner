package com.algos.stockscanner.data.service;

import com.algos.stockscanner.beans.Utils;
import com.algos.stockscanner.data.entity.Generator;
import com.algos.stockscanner.data.entity.MarketIndex;
import com.algos.stockscanner.data.entity.Simulation;
import com.algos.stockscanner.views.simulations.SimulationModel;
import com.vaadin.flow.data.provider.QuerySortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.vaadin.artur.helpers.CrudService;

import java.util.ArrayList;
import java.util.List;

@Service
public class SimulationService extends CrudService<Simulation, Integer> {

    @Autowired
    private Utils utils;

    private SimulationRepository repository;

    public SimulationService(@Autowired SimulationRepository repository) {
        this.repository = repository;
    }


    public List<SimulationModel> fetch(int offset, int limit, Example<Simulation> example, List<QuerySortOrder> orders) {

        Sort sort=utils.buildSort(orders);

        Pageable pageable = new OffsetBasedPageRequest(offset, limit, sort);

        Page<Simulation> page;
        if(example!=null){
            page = repository.findAll(example, pageable);
        }else{
            page = repository.findAll(pageable);
        }

        List<SimulationModel> list = new ArrayList<>();
        for(Simulation entity : page.toList()){
            SimulationModel model = new SimulationModel();
            entityToModel(entity, model);
            list.add(model);
        }

        return list;
    }

//    private Sort buildSort(List<QuerySortOrder> orders){
//
//        List<Sort.Order> sortOrders = new ArrayList<>();
//
//        for(QuerySortOrder order : orders){
//
//            SortDirection sortDirection = order.getDirection();
//            String sortProperty = order.getSorted();
//
//            Sort.Direction sDirection=null;
//            switch (sortDirection){
//                case ASCENDING:
//                    sDirection=Sort.Direction.ASC;
//                    break;
//                case DESCENDING:
//                    sDirection=Sort.Direction.DESC;
//                    break;
//            }
//
//            sortOrders.add(new Sort.Order(sDirection, sortProperty));
//
//        }
//
//        return Sort.by(sortOrders);
//    }

    public int count(Example<Simulation> example) {
        return (int)repository.count(example);
    }

    public int count() {
        return (int)repository.count();
    }

    public int countBy(Generator generator) {
        Simulation entity = new Simulation();
        entity.setGenerator(generator);
        return (int)repository.count(Example.of(entity));
    }




    @Override
    protected SimulationRepository getRepository() {
        return repository;
    }


    /**
     * Copy data from Entity to View Model
     */
    private void entityToModel(Simulation entity, SimulationModel model){
        model.setId(utils.toPrimitive(entity.getId()));

        if(entity.getIndex()!=null){
            model.setId(entity.getId());
            Generator gen = entity.getGenerator();
            if(gen!=null){
                model.setNumGenerator(utils.toPrimitive(gen.getNumber()));
            }
            MarketIndex index=entity.getIndex();
            if(index!=null){
                model.setSymbol(index.getSymbol());
            }
            model.setStartTs(entity.getStartTsLD());
            model.setEndTs(entity.getEndTsLD());
            model.setInitialAmount(utils.toPrimitive(entity.getInitialAmount()));
            model.setAmplitude(utils.toPrimitive(entity.getAmplitude()));
            model.setFinalAmount(utils.toPrimitive(entity.getFinalAmount()));
            model.setTerminationCode(entity.getTerminationCode());
            model.setTotSpread(utils.toPrimitive(entity.getTotSpread()));
            model.setTotCommission(utils.toPrimitive(entity.getTotCommission()));
            model.setPl(utils.toPrimitive(entity.getPl()));
            model.setPlPercent(utils.toPrimitive(entity.getPlPercent()));
            model.setNumPointsScanned(utils.toPrimitive(entity.getNumPointsTotal()));
            model.setNumPointsHold(utils.toPrimitive(entity.getNumPointsOpen()));
            model.setNumPointsWait(utils.toPrimitive(entity.getNumPointsClosed()));
            model.setMinPointsHold(utils.toPrimitive(entity.getShortestPeriodOpen()));
            model.setMaxPointsHold(utils.toPrimitive(entity.getLongestPeriodOpen()));
        }

    }

//    public void deleteByGenerator(Generator generator) {
//        repository.deleteByGenerator(generator);
//    }

}
