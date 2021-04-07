package com.algos.stockscanner.data.service;

import com.algos.stockscanner.data.entity.Simulation;
import com.algos.stockscanner.views.simulations.SimulationModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.vaadin.artur.helpers.CrudService;

import java.util.ArrayList;
import java.util.List;

@Service
public class SimulationService extends CrudService<Simulation, Integer> {

    private SimulationRepository repository;

    public SimulationService(@Autowired SimulationRepository repository) {
        this.repository = repository;
    }


    public List<SimulationModel> fetch(int offset, int limit) {

        Pageable pageable = new OffsetBasedPageRequest(offset, limit);
        Page<Simulation> page = repository.findAll(pageable);
        List<SimulationModel> list = new ArrayList<>();
        for(Simulation entity : page.toList()){
            SimulationModel model = new SimulationModel();
            entityToModel(entity, model);
            list.add(model);
        }

        return list;
    }

    public int count() {
        return (int)repository.count();
    }


    @Override
    protected SimulationRepository getRepository() {
        return repository;
    }


    /**
     * Copy data from Entity to View Model
     * */
    private void entityToModel(Simulation entity, SimulationModel model){
        model.setId(entity.getId());

        if(entity.getMarketIndex()!=null){
            model.setSymbol(entity.getMarketIndex().getSymbol());
            model.setImageData(entity.getMarketIndex().getImage());
        }

    }

}
