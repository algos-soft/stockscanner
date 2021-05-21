package com.algos.stockscanner.beans;

import com.algos.stockscanner.data.entity.MarketIndex;
import com.algos.stockscanner.data.service.MarketIndexService;
import com.algos.stockscanner.enums.PriceUpdateModes;
import com.algos.stockscanner.services.AdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@EnableAsync
public class Scheduler {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Value("${alphavantage.max.requests.per.minute:5}")
    private int MAX_REQ_PER_MINUTE;

    @Value("${price.update.enable}")
    private boolean priceUpdateEnable;

    @Value("${price.update.max.per.cycle:100}")
    private int priceUpdateMaxPerCycle;



    @Autowired
    private AdminService adminService;

    @Autowired
    private MarketIndexService marketIndexService;

    @Async
    @Scheduled(cron = "${price.update.cron}")
    public void schedulePriceUpdates(){
        if(priceUpdateEnable){
            log.info("price update scheduler triggered");
            launchPriceUpdate();
        }
    }


    /**
     * Retrieve the indexes with the oldest update date and update them.
     * This updates the indexes in a rotation.
     */
    private void launchPriceUpdate(){
        List<MarketIndex> marketIndexes = marketIndexService.findAllOrderByUnitsToLimit(priceUpdateMaxPerCycle);
        List<String> symbols=new ArrayList<>();
        marketIndexes.stream().forEach(marketIndex -> symbols.add(marketIndex.getSymbol()));
        adminService.scheduleUpdate(symbols, PriceUpdateModes.ADD_MISSING_DATA_ONLY, MAX_REQ_PER_MINUTE);
    }

}
