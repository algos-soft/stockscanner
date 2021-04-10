package com.algos.stockscanner.data.generator;

import com.algos.stockscanner.Application;
import com.algos.stockscanner.beans.Utils;
import com.algos.stockscanner.data.entity.MarketIndex;
import com.algos.stockscanner.data.service.MarketIndexService;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.spring.annotation.SpringComponent;

import com.algos.stockscanner.data.service.SimulationRepository;
import com.algos.stockscanner.data.entity.Simulation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.vaadin.artur.exampledata.DataType;
import org.vaadin.artur.exampledata.ExampleDataGenerator;

@SpringComponent
public class DataGenerator {

    @Autowired
    private MarketIndexService marketIndexService;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private Utils utils;

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

            logger.info("... generating Simulation entities...");
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
            simulationRepository.saveAll(simulationRepositoryGenerator.create(200, seed));

            if(marketIndexService.count()==0){
                generateMarketIndexes();
            }



            logger.info("Generated demo data");
        };
    }

    private void generateMarketIndexes(){
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

    private byte[] retrieveImage(String url){
        byte[] imageData = new byte[0];
        try {
            imageData = utils.getIconFromUrl(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imageData;
    }

}