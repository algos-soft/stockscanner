package com.algos.stockscanner.data.service;

import com.algos.stockscanner.data.entity.Simulation;

import com.algos.stockscanner.views.simulations.SimulationModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vaadin.artur.helpers.CrudService;

import java.time.LocalDate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class SimulationService extends CrudService<Simulation, Integer> {

    private SimulationRepository repository;

    public SimulationService(@Autowired SimulationRepository repository) {
        this.repository = repository;
    }


    public List<SimulationModel> fetch(int offset, int limit) {
        SimulationModel model;
        List<SimulationModel> list = new ArrayList<>();
        model=new SimulationModel();
        model.setSymbol("TSLA");
        list.add(model);

        model=new SimulationModel();
        model.setSymbol("IBM");
        list.add(model);

        model=new SimulationModel();
        model.setSymbol("MSFT");
        list.add(model);

        return list;
    }

    public int count() {
        return 3;
    }


    @Override
    protected SimulationRepository getRepository() {
        return repository;
    }

}
