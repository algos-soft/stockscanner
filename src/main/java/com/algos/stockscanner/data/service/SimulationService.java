package com.algos.stockscanner.data.service;

import com.algos.stockscanner.data.entity.Simulation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vaadin.artur.helpers.CrudService;
import java.time.LocalDate;
import java.time.LocalDate;

@Service
public class SimulationService extends CrudService<Simulation, Integer> {

    private SimulationRepository repository;

    public SimulationService(@Autowired SimulationRepository repository) {
        this.repository = repository;
    }

    @Override
    protected SimulationRepository getRepository() {
        return repository;
    }

}
