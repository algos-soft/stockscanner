package com.algos.stockscanner.data.service;

import com.algos.stockscanner.beans.Utils;
import com.algos.stockscanner.data.entity.*;
import com.algos.stockscanner.data.enums.ActionTypes;
import com.algos.stockscanner.data.enums.Actions;
import com.algos.stockscanner.data.enums.Reasons;
import com.algos.stockscanner.views.simulations.SimulationItemModel;
import com.vaadin.flow.data.provider.QuerySortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.vaadin.artur.helpers.CrudService;

import java.util.ArrayList;
import java.util.List;

@Service
public class SimulationItemService extends CrudService<SimulationItem, Integer> {

    private SimulationItemRepository repository;

    @Autowired
    private Utils utils;

    public SimulationItemService(@Autowired SimulationItemRepository repository) {
        this.repository = repository;
    }


    public List<SimulationItemModel> fetch(int offset, int limit, Example<SimulationItem> example, List<QuerySortOrder> orders) {

        Sort sort=utils.buildSort(orders);

        Pageable pageable = new OffsetBasedPageRequest(offset, limit, sort);

        Page<SimulationItem> page;
        if(example!=null){
            page = repository.findAll(example, pageable);
        }else{
            page = repository.findAll(pageable);
        }

        List<SimulationItemModel> list = new ArrayList<>();
        for(SimulationItem entity : page.toList()){
            SimulationItemModel model = new SimulationItemModel();
            entityToModel(entity, model);
            list.add(model);
        }

        return list;
    }

    public List<SimulationItemModel> findBySimulationOrderByTimestamp(Simulation simulation){
        List<SimulationItem> items = repository.findBySimulationOrderByTimestamp(simulation);
        List<SimulationItemModel> modelItems=new ArrayList<>();
        for(SimulationItem entity : items){
            SimulationItemModel model = new SimulationItemModel();
            entityToModel(entity, model);
            modelItems.add(model);
        }
        return modelItems;
    }

    public int count(Example<SimulationItem> example) {
        return (int)repository.count(example);
    }

    /**
     * Copy data from Entity to View Model
     */
    private void entityToModel(SimulationItem entity, SimulationItemModel model){

        model.setId(utils.toPrimitive(entity.getId()));
        model.setTimestamp(entity.getTimestampLDT());
        model.setAction(Actions.get(entity.getAction()));
        model.setActionType(ActionTypes.get(entity.getActionType()));
        model.setReason(Reasons.get(entity.getReason()));
        model.setRefPrice(utils.toPrimitive(entity.getRefPrice()));
        model.setCurrPrice(utils.toPrimitive(entity.getCurrPrice()));
        model.setDeltaAmpl(utils.toPrimitive(entity.getDeltaAmpl()));
        model.setSpreadAmt(utils.toPrimitive(entity.getSpreadAmt()));
        model.setCommissionAmt(utils.toPrimitive(entity.getCommissionAmt()));
        model.setCurrValue(utils.toPrimitive(entity.getCurrValue()));
        model.setPl(utils.toPrimitive(entity.getPl()));

    }


    @Override
    protected SimulationItemRepository getRepository() {
        return repository;
    }

}
