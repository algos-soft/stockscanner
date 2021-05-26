package com.algos.stockscanner.strategies;

import com.algos.stockscanner.data.entity.MarketIndex;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * The amplitudeUp and amplitudeDown are recalculated at each cycle in order to
 * match the general trend (the slope determined by the variation of the moving average)
 */
@Component("surferStrategyVariableAmplitude")
@Scope("prototype")
public class SurferStrategyVariableAmplitude extends SurferStrategy {

    public SurferStrategyVariableAmplitude(MarketIndex index, LocalDate startDate, int numDays, float initialAmount, int sl, float amplitude, int daysLookback) {
        super(index, startDate, numDays, initialAmount, sl, amplitude, daysLookback);
    }


    @Override
    void preProcessUnit() {

        LocalDateTime t0 = unit.getDateTimeLDT();   // now
        LocalDateTime t2 = t0.minusDays(daysLookback);  // one period back
        LocalDateTime t1 = t2.minusDays(daysLookback);  // two periods back
        float ma1 = indexUnitService.getMovingAverage(index, t1, t2);   // moving average 2 periods back
        float ma2 = movingAverage(daysLookback);    // moving average 1 period back
        float maDeltaFactor=ma2/ma1;    // how the moving average is changing

        // reflect the trend of the moving average on the up/down balance
        float delta = -amplitude*(1-maDeltaFactor);
        amplitudeUp=amplitude+delta;
        amplitudeDown=amplitude-delta;

    }

}
