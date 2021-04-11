package com.algos.stockscanner.data.datagenerator;

import com.algos.stockscanner.beans.Utils;
import com.algos.stockscanner.data.entity.Generator;
import com.algos.stockscanner.data.entity.MarketIndex;
import com.algos.stockscanner.data.service.GeneratorService;
import com.algos.stockscanner.data.service.MarketIndexService;
import com.algos.stockscanner.data.service.SimulationService;
import com.vaadin.flow.spring.annotation.SpringComponent;

import com.algos.stockscanner.data.service.SimulationRepository;
import com.algos.stockscanner.data.entity.Simulation;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.vaadin.artur.exampledata.DataType;
import org.vaadin.artur.exampledata.ExampleDataGenerator;

@SpringComponent
public class DataGenerator {

    @Autowired
    private MarketIndexService marketIndexService;

    @Autowired
    private GeneratorService generatorService;

    @Autowired
    private SimulationService simulationService;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private Utils utils;

    @Bean
    public CommandLineRunner loadData(SimulationRepository simulationRepository) {
        return args -> {
            Logger logger = LoggerFactory.getLogger(getClass());
//            if (simulationRepository.count() != 0L) {
//                logger.info("Using existing database");
//                return;
//            }
//            int seed = 123;

            logger.info("Generating demo data");

//            logger.info("... generating Simulation entities...");
//            ExampleDataGenerator<Simulation> simulationRepositoryGenerator = new ExampleDataGenerator<>(
//                    Simulation.class, LocalDateTime.of(2021, 4, 1, 0, 0, 0));
//            simulationRepositoryGenerator.setData(Simulation::setId, DataType.ID);
//            //simulationRepositoryGenerator.setData(Simulation::setIndex, DataType.WORD);
//            simulationRepositoryGenerator.setData(Simulation::setStartTs, DataType.DATE_OF_BIRTH);
//            simulationRepositoryGenerator.setData(Simulation::setEndTs, DataType.DATE_OF_BIRTH);
////            simulationRepositoryGenerator.setData(Simulation::setInitialAmount, DataType.NUMBER_UP_TO_1000);
//            simulationRepositoryGenerator.setData(Simulation::setLeverage, DataType.NUMBER_UP_TO_100);
////            simulationRepositoryGenerator.setData(Simulation::setWidth, DataType.NUMBER_UP_TO_100);
////            simulationRepositoryGenerator.setData(Simulation::setBalancing, DataType.NUMBER_UP_TO_10);
////            simulationRepositoryGenerator.setData(Simulation::setNum_buy, DataType.NUMBER_UP_TO_100);
////            simulationRepositoryGenerator.setData(Simulation::setNum_sell, DataType.NUMBER_UP_TO_100);
////            simulationRepositoryGenerator.setData(Simulation::setPl_percent, DataType.NUMBER_UP_TO_100);
//            simulationRepository.saveAll(simulationRepositoryGenerator.create(200, seed));

            if (marketIndexService.count() == 0) {
                generateMarketIndexes();
            }

            if (generatorService.count() == 0) {
                generateGenerators();
            }

            if (simulationService.count() == 0) {
                generateSimulations();
            }

            logger.info("Generated demo data");
        };
    }

    private void generateMarketIndexes() {
        Logger logger = LoggerFactory.getLogger(getClass());

        logger.info("... generating MarketIndex entities...");

        MarketIndex index;

        index = new MarketIndex();
        index.setSymbol("TSLA");
        index.setName("Tesla");
        index.setImage(retrieveImage("https://etoro-cdn.etorostatic.com/market-avatars/tsla/150x150.png"));
        index.setCategory("STOCK");
        marketIndexService.update(index);

        index = new MarketIndex();
        index.setSymbol("MSFT");
        index.setName("Microsoft");
        index.setImage(retrieveImage("https://etoro-cdn.etorostatic.com/market-avatars/msft/150x150.png"));
        index.setCategory("STOCK");
        marketIndexService.update(index);

        index = new MarketIndex();
        index.setSymbol("IBM");
        index.setName("Ibm");
        index.setImage(retrieveImage("https://etoro-cdn.etorostatic.com/market-avatars/ibm/150x150.png"));
        index.setCategory("STOCK");
        marketIndexService.update(index);

        index = new MarketIndex();
        index.setSymbol("AAPL");
        index.setName("Apple");
        index.setImage(retrieveImage("https://etoro-cdn.etorostatic.com/market-avatars/aapl/150x150.png"));
        index.setCategory("STOCK");
        marketIndexService.update(index);

        index = new MarketIndex();
        index.setSymbol("MRK");
        index.setName("Merck");
        index.setImage(retrieveImage("https://etoro-cdn.etorostatic.com/market-avatars/mrk/150x150.png"));
        index.setCategory("STOCK");
        marketIndexService.update(index);

        index = new MarketIndex();
        index.setSymbol("NFLX");
        index.setName("Netflix");
        index.setImage(retrieveImage("https://etoro-cdn.etorostatic.com/market-avatars/nflx/150x150.png"));
        index.setCategory("STOCK");
        marketIndexService.update(index);

        index = new MarketIndex();
        index.setSymbol("NKE");
        index.setName("Nike");
        index.setImage(retrieveImage("https://etoro-cdn.etorostatic.com/market-avatars/nke/150x150.png"));
        index.setCategory("STOCK");
        marketIndexService.update(index);

        index = new MarketIndex();
        index.setSymbol("MARA");
        index.setName("Marathon Patent Group Inc");
        index.setImage(retrieveImage("https://etoro-cdn.etorostatic.com/market-avatars/6244/150x150.png"));
        index.setCategory("STOCK");
        marketIndexService.update(index);

        logger.info("Generated MarketIndex entities");

    }


    private void generateGenerators() {
        Logger logger = LoggerFactory.getLogger(getClass());
        logger.info("... generating Generator entities...");

        List<MarketIndex> indexes = marketIndexService.findAll();

        Generator gen;

        gen = new Generator();
        generatorService.initEntity(gen);
        gen.setIndex(pickRandomIndex(indexes));
        gen.setStartDate(LocalDate.now());
        gen.setAmount(500);
        generatorService.update(gen);

        gen = new Generator();
        generatorService.initEntity(gen);
        gen.setIndex(pickRandomIndex(indexes));
        gen.setStartDate(LocalDate.now());
        gen.setAmount(2000);
        generatorService.update(gen);

        gen = new Generator();
        generatorService.initEntity(gen);
        gen.setIndex(pickRandomIndex(indexes));
        gen.setStartDate(LocalDate.now());
        gen.setAmount(800);
        generatorService.update(gen);

        logger.info("Generated Generator entities");

    }


    private void generateSimulations() {
        Logger logger = LoggerFactory.getLogger(getClass());

        logger.info("... generating Simulation entities...");

        List<Generator> generators = generatorService.findAll();

        Simulation sim;

        sim = new Simulation();
        sim.setGenerator(pickRandomGenerator(generators));
        sim.setIndex(sim.getGenerator().getIndex());
        sim.setStartTs(LocalDate.now().minusYears(1));
        sim.setEndTs(sim.getStartTs().plusDays(30));
        sim.setLeverage(1);
        sim.setAmplitude(10f);
        sim.setInitialAmount(500f);
        sim.setFinalAmount(550f);
        simulationService.update(sim);

        sim = new Simulation();
        sim.setGenerator(pickRandomGenerator(generators));
        sim.setIndex(sim.getGenerator().getIndex());
        sim.setStartTs(LocalDate.now().minusYears(1));
        sim.setEndTs(sim.getStartTs().plusDays(20));
        sim.setLeverage(2);
        sim.setAmplitude(10f);
        sim.setInitialAmount(300f);
        sim.setFinalAmount(350f);
        simulationService.update(sim);

        sim = new Simulation();
        sim.setGenerator(pickRandomGenerator(generators));
        sim.setIndex(sim.getGenerator().getIndex());
        sim.setStartTs(LocalDate.now().minusYears(1));
        sim.setEndTs(sim.getStartTs().plusDays(45));
        sim.setLeverage(1);
        sim.setAmplitude(25f);
        sim.setInitialAmount(600f);
        sim.setFinalAmount(550f);
        simulationService.update(sim);

        sim = new Simulation();
        sim.setGenerator(pickRandomGenerator(generators));
        sim.setIndex(sim.getGenerator().getIndex());
        sim.setStartTs(LocalDate.now().minusYears(1));
        sim.setEndTs(sim.getStartTs().plusDays(20));
        sim.setLeverage(1);
        sim.setAmplitude(15f);
        sim.setInitialAmount(1000f);
        sim.setFinalAmount(1200f);
        simulationService.update(sim);

        sim = new Simulation();
        sim.setGenerator(pickRandomGenerator(generators));
        sim.setIndex(sim.getGenerator().getIndex());
        sim.setStartTs(LocalDate.now().minusYears(2));
        sim.setEndTs(sim.getStartTs().plusDays(40));
        sim.setLeverage(1);
        sim.setAmplitude(10f);
        sim.setInitialAmount(1800f);
        sim.setFinalAmount(1600f);
        simulationService.update(sim);


        logger.info("Generated Simulation entities");

    }


    private MarketIndex pickRandomIndex(List<MarketIndex> items) {
        Random rand = new Random();
        return items.get(rand.nextInt(items.size()));
    }

    private Generator pickRandomGenerator(List<Generator> items) {
        Random rand = new Random();
        return items.get(rand.nextInt(items.size()));
    }



    private byte[] retrieveImage(String url) {
        byte[] imageData = new byte[0];
        try {
            imageData = utils.getIconFromUrl(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imageData;
    }

}