package com.algos.stockscanner.data.generator;

import com.vaadin.flow.spring.annotation.SpringComponent;

import com.algos.stockscanner.data.service.SimulationRepository;
import com.algos.stockscanner.data.entity.Simulation;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.vaadin.artur.exampledata.DataType;
import org.vaadin.artur.exampledata.ExampleDataGenerator;

@SpringComponent
public class DataGenerator {

    @Bean
    public CommandLineRunner loadData(SimulationRepository simulationRepository) {
        return args -> {
            Logger logger = LoggerFactory.getLogger(getClass());
            if (simulationRepository.count() != 0L) {
                logger.info("Using existing database");
                return;
            }
            int seed = 123;

            logger.info("Generating demo data");

            logger.info("... generating 100 Simulation entities...");
            ExampleDataGenerator<Simulation> simulationRepositoryGenerator = new ExampleDataGenerator<>(
                    Simulation.class, LocalDateTime.of(2021, 4, 1, 0, 0, 0));
            simulationRepositoryGenerator.setData(Simulation::setId, DataType.ID);
            //simulationRepositoryGenerator.setData(Simulation::setIndex, DataType.WORD);
            simulationRepositoryGenerator.setData(Simulation::setStartTs, DataType.DATE_OF_BIRTH);
            simulationRepositoryGenerator.setData(Simulation::setEndTs, DataType.DATE_OF_BIRTH);
//            simulationRepositoryGenerator.setData(Simulation::setInitialAmount, DataType.NUMBER_UP_TO_1000);
            simulationRepositoryGenerator.setData(Simulation::setLeverage, DataType.NUMBER_UP_TO_100);
//            simulationRepositoryGenerator.setData(Simulation::setWidth, DataType.NUMBER_UP_TO_100);
//            simulationRepositoryGenerator.setData(Simulation::setBalancing, DataType.NUMBER_UP_TO_10);
//            simulationRepositoryGenerator.setData(Simulation::setNum_buy, DataType.NUMBER_UP_TO_100);
//            simulationRepositoryGenerator.setData(Simulation::setNum_sell, DataType.NUMBER_UP_TO_100);
//            simulationRepositoryGenerator.setData(Simulation::setPl_percent, DataType.NUMBER_UP_TO_100);
            simulationRepository.saveAll(simulationRepositoryGenerator.create(100, seed));

            logger.info("Generated demo data");
        };
    }

}