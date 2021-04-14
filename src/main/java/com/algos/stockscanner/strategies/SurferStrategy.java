package com.algos.stockscanner.strategies;

import com.algos.stockscanner.data.entity.IndexUnit;
import com.algos.stockscanner.data.entity.MarketIndex;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Scope("prototype")
public class SurferStrategy extends AbsStrategy {

    @Override
    public String getCode() {
        return "SURFER";
    }

    public SurferStrategy(StrategyParams params) {
        super(params);
    }

    @Override
    public void processUnit() throws Exception {
        Thread.sleep(5);
        System.out.println(unitIndex+" "+unit.getId()+" "+unit.getDateTime());
    }

    @Override
    public boolean isFinished() {



        return false;
    }


}
