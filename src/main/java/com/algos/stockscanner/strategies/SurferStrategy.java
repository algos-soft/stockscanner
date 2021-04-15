package com.algos.stockscanner.strategies;

import com.algos.stockscanner.beans.Utils;
import com.algos.stockscanner.data.entity.Generator;
import com.algos.stockscanner.data.entity.IndexUnit;
import com.algos.stockscanner.data.entity.MarketIndex;
import com.algos.stockscanner.data.enums.Terminations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Scope("prototype")
public class SurferStrategy extends AbsStrategy {

    @Autowired
    Utils utils;

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


    /**
     * @return null if not finished, the termination reason if finished
     */
    @Override
    public Terminations isFinished() {

        // if 1) fixed days or 2) variable days with max specified, then check the max day
        boolean fixedDays = params.isFixedDays();
        LocalDate endDate=params.getEndDate();
        if(fixedDays || (!fixedDays && endDate!=null)){
            LocalDate unitDate = unit.getDateTimeLDT().toLocalDate();
            if(unitDate.isAfter(endDate)){
                return Terminations.MAX_DAYS_REACHED;
            }
        }






        return null;
    }


}
