package com.algos.stockscanner.strategies;

import com.algos.stockscanner.beans.Utils;
import com.algos.stockscanner.data.entity.IndexUnit;
import com.algos.stockscanner.data.entity.MarketIndex;
import com.algos.stockscanner.data.entity.Simulation;
import com.algos.stockscanner.data.entity.SimulationItem;
import com.algos.stockscanner.data.service.GeneratorService;
import com.algos.stockscanner.data.service.IndexUnitService;
import com.algos.stockscanner.data.service.SimulationItemService;
import com.algos.stockscanner.data.service.SimulationService;
import com.algos.stockscanner.enums.ActionTypes;
import com.algos.stockscanner.enums.Actions;
import com.algos.stockscanner.enums.Reasons;
import com.algos.stockscanner.enums.Terminations;
import com.algos.stockscanner.utils.CpuMonitorListener;
import com.algos.stockscanner.utils.CpuMonitorTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TimerTask;

public abstract class AbsStrategy implements Strategy {

    private static final Logger log = LoggerFactory.getLogger(AbsStrategy.class);

    private static final int PAGE_SIZE = 100;      // max units per page

//    StrategyParams params;
    MarketIndex index;
    LocalDate startDate;
    int numDays;
    float initialAmount;

    LocalDate endDate;

    boolean abort=false;

    Simulation simulation;

    // current page of units scanned
    List<IndexUnit> unitsPage=new ArrayList<>();

    // Index of the current unit in the units page, 0 for the first
    int unitIndex=0;

    // total number of points scanned
    int totPoints=0;

    // total number of points with position open
    int totPointsOpen=0;

    // total number of points with position closed
    int totPointsClosed=0;

    // Currently scanned unit
    IndexUnit unit;

    // the reason why has terminated
    Terminations termination;

    boolean posOpen; //a position is open

    ActionTypes posType; // if open for buy or sell

    float openPrice;    // opened at this price
    float openValue;    // value of the position when opened
    float currValue;    // current value updated while position is open
    float lastPrice;    // price at the previous point scanned
    float lastValue;    // value at the previous point scanned
    IndexUnit lastUnit; // previous index unit scanned
    float totPl;        // total P/L of the simulation
    int totOpenings;    // total times a position has been opened

    List<StrategyProgressListener> listeners=new ArrayList<>();

    private int cpuPauseMs;

    @Autowired
    private ApplicationContext context;

    @Autowired
    IndexUnitService indexUnitService;

    @Autowired
    SimulationService simulationService;

    @Autowired
    GeneratorService generatorService;

    @Autowired
    SimulationItemService simulationItemService;

    @Autowired
    StrategyService strategyService;

    @Autowired
    private Utils utils;


    public AbsStrategy(MarketIndex index, LocalDate startDate, int numDays, float initialAmount) {
        this.index=index;
        this.startDate=startDate;
        this.numDays=numDays;
        this.initialAmount=initialAmount;
        this.endDate=startDate.plusDays(numDays);
    }

    @PostConstruct
    private void init(){
    }

    @Override
    public void addProgressListener(StrategyProgressListener listener) {
        listeners.add(listener);
    }

    private void notifyProgress(int current, int total, String status){
        for(StrategyProgressListener listener : listeners){
            listener.notifyProgress(current, total, status);
        }
    }

    public Simulation execute() throws Exception {

        // create a new Simulation
        simulation = new Simulation();
        simulation.setIndex(index);
        simulation.setStartTsLDT(startDate.atStartOfDay());
        simulation.setInitialAmount(initialAmount);

        notifyProgress(0, 1, ""+this);

        // you can exit this cycle only with a termination code assigned
        do {

            if(abort){
                termination=Terminations.ABORTED_BY_USER;
                break;
            }

            // apply retroaction to cpu load
            if(cpuPauseMs >0){
                Thread.sleep(cpuPauseMs);
            }

            if(ensureUnitsAvailable()){

                unit = unitsPage.get(unitIndex);

                Terminations term = isFinished();
                if(term==null){

                    SimulationItem item = processUnit();
                    simulation.getSimulationItems().add(item);

                } else {

                    termination=term;
                    if(posOpen){
                        forceCloseCurrentSimulation(termination);
                    }
                    break;

                }


            }else{

                termination =Terminations.NO_MORE_DATA;
                if(posOpen){
                    forceCloseCurrentSimulation(termination);
                }
                break;
            }

            unitIndex++;
            notifyProgress(unitIndex, numDays, ""+this);

        } while (true);

        notifyProgress(1, 1, ""+this);

        // consolidate data in the simulation
        consolidate();

        return simulation;

    }

    // if we have an open position we must close it - we add an extra close line at the last timestamp
    private void forceCloseCurrentSimulation(Terminations term){
        // reset to previous values, so everything behaves as in the previous line
        currValue=lastValue;
        unit=lastUnit;

        Reasons reason = Reasons.get(term.getCode());;
        Decision decision=new Decision(Actions.CLOSE, null, reason);
        DecisionInfo decisionInfo=new DecisionInfo();
        decisionInfo.setRefPrice(openPrice);
        decisionInfo.setCurrPrice(unit.getClose());
        decisionInfo.setDeltaAmpl(strategyService.deltaPercent(openPrice, unit.getClose()));
        decisionInfo.setTimestamp(unit.getDateTime());
        decision.setDecisionInfo(decisionInfo);

        closePosition(decisionInfo);

        SimulationItem item = strategyService.buildSimulationItem(simulation, decision, unit);
        simulation.getSimulationItems().add(item);

    }


    /**
     * Ensure that we still have units available in the current page.
     * If not, load another page.
     */
    private boolean ensureUnitsAvailable(){

        if(unitIndex<unitsPage.size()){   // requested unit index is available
            return true;
        }else{  // need to load a new page
            if(unit!=null){   // we have a previous unit, start from the next one
                int unitId=unit.getId();
                List<IndexUnit> units = indexUnitService.findAllByIndexFromId(index, unitId+1, PAGE_SIZE);
                unitsPage.clear();
                if(units.size()>0){
                    unitsPage.addAll(units);
                    unitIndex=0;
                    return true;
                }else{  // no more units available in the index
                    return false;
                }
            }else{  // no previous unit, it is the first time
                int firstId = indexUnitService.findFirstIdOf(index, startDate);
                unitsPage.clear();
                if(firstId>0){
                    List<IndexUnit> units = indexUnitService.findAllByIndexFromId(index, firstId, PAGE_SIZE);
                    unitsPage.addAll(units);
                    return true;
                } else {
                    return false;
                }
            }
        }

    }

    @Override
    public void abort() {
        abort=true;
    }


    @Override
    public SimulationItem processUnit() throws Exception {

        preProcessUnit();   // chance for subclasses to do something before processint the unit

        Decision decision = takeDecision();
        Actions action = decision.getAction();

        log.debug(decision.toString());

        switch (action) {

            case OPEN:

                if(posOpen){
                    throw new Exception("Position is already open, you can't open it again");
                }

                switch (decision.getActionType()){
                    case BUY:
                        posType=ActionTypes.BUY;
                        break;
                    case SELL:
                        posType=ActionTypes.SELL;
                        break;
                }

                openPrice=unit.getClose();
                lastPrice=openPrice;
                openValue=initialAmount;
                currValue=openValue;
                lastValue=currValue;

                // if buy, apply spread at open
                if(posType==ActionTypes.BUY){
                    float spreadValue = applySpread();
                    decision.getDecisionInfo().setSpreadAmt(spreadValue);
                }

                updatePosition(decision.getDecisionInfo());

                totPointsOpen++;
                posOpen=true;
                totOpenings++;
                break;

            case CLOSE:

                if(!posOpen){
                    throw new Exception("Position is already closed, you can't close it again");
                }

                // if sell, apply spread at close
                if(posType==ActionTypes.SELL){
                    float spreadValue = applySpread();
                    decision.getDecisionInfo().setSpreadAmt(spreadValue);
                }

                closePosition(decision.getDecisionInfo());

                totPointsClosed++;
                break;

            case STAY:
                if(posOpen){
                    updatePosition(decision.getDecisionInfo());
                    totPointsOpen++;
                }else{
                    totPointsClosed++;
                }
                break;
        }


        // count scanned points by type
        totPoints++;

        // build the simulation item
        SimulationItem simulationItem = strategyService.buildSimulationItem(simulation, decision, unit);

        // save last price - at the end!
        lastPrice=unit.getClose();
        lastValue=currValue;
        lastUnit=unit;

        return simulationItem;

    }

    /**
     * chance for subclasses to do something before processing the unit
     */
    void preProcessUnit(){};

    /**
     * reduces the current value by the value of the spread
     *
     * @return the value of the spread
     */
    private float applySpread(){
        float spread = utils.toPrimitive(index.getSpreadPercent());
        float spreadAmt = -strategyService.calcPercent(currValue,spread);
        currValue+=spreadAmt;
        return spreadAmt;
    }

    private void closePosition(DecisionInfo decisionInfo){
        updatePosition(decisionInfo);
        float pl = currValue-openValue; // P/L of this spread
        decisionInfo.setPl(pl);
        totPl+=pl;  // increment tot P/L of the simulation
        posOpen =false;
    }


    /**
     * Take a decision based on the current state
     *
     * Warning: current price / value stored in variables has not been
     * calculated yet, and it refers to the previous unit.
     * If you need to evaluate actual values, use the values from the current unit instead.
     */
    @Override
    public Decision takeDecision() {
        Decision decision=null;

        if(posOpen){    // position is open
            switch (posType){
                case BUY:
                    decision = decideIfCloseBuyPosition();
                    break;
                case SELL:
                    decision = decideIfCloseSellPosition();
                    break;
            }
        } else {    // position is not opened
            decision = decideIfOpenPosition();
        }

        return decision;
    }


//    private void openBuy(){
//        posType=ActionTypes.BUY;
//    }
//
//    private void openSell(){
//        posType=ActionTypes.SELL;
//    }
//
//    private void closePosition(){
//        posType=null;
//        openPrice=0;
//    }

//    /**
//     * update current amount
//     */
//    private void updatePositionOld(DecisionInfo decisionInfo){
//        currValue = currValue + calcDeltaValue();
//        decisionInfo.setCurrValue(currValue);
//    }

    /**
     * update current amount
     */
    private void updatePosition(DecisionInfo decisionInfo){
        currValue=calcCurrValue();
        decisionInfo.setCurrValue(currValue);
    }


    float calcCurrValue(){
        float value=0;
        float diffPrice=unit.getClose()-openPrice;
        float diffValue=diffPrice * openValue / openPrice;
        switch (posType){
            case BUY:
                value=openValue+diffValue;
                break;
            case SELL:
                value=openValue-diffValue;
                break;
        }
        return value;
    }




    /**
     * consolidate data in the simulation
     */
    private void consolidate(){
        simulation.setTerminationCode(termination.getCode());
        if(unit!=null){
            simulation.setEndTsLDT(unit.getDateTimeLDT());
        }else{
            simulation.setEndTsLDT(simulation.getStartTsLDT());
        }
        simulation.setPl(totPl);
        simulation.setPlPercent(strategyService.deltaPercent(initialAmount, initialAmount+totPl));
        simulation.setNumPointsTotal(totPoints);
        simulation.setNumOpenings(totOpenings);
        simulation.setNumPointsOpen(totPointsOpen);
        simulation.setNumPointsClosed(totPointsClosed);

        TwoIntegers result=calcShortLongPeriods();
        simulation.setShortestPeriodOpen(result.getI1());
        simulation.setLongestPeriodOpen(result.getI2());
    }


    /**
     * @return the shortest and the longest period open
     */
    private TwoIntegers calcShortLongPeriods(){
        int shortest=0;
        int longest=0;
        ArrayList<Integer> lengths = new ArrayList();
        List<SimulationItem> simulationItems=simulation.getSimulationItems();
        int counter=0;
        boolean open=false;
        for(SimulationItem item : simulationItems){
            Actions action = Actions.get(item.getAction());
            switch (action){
                case OPEN:
                    open=true;
                    counter=1;
                    break;
                case CLOSE:
                    lengths.add(Integer.valueOf(counter));
                    counter=0;
                    open=false;
                    break;
                case STAY:
                    if(open){
                        counter++;
                    }
                    break;
            }
        }

        if(lengths.size()>0){
            Collections.sort(lengths);
            shortest=lengths.get(0);
            longest=lengths.get(lengths.size()-1);
        }

        return new TwoIntegers(shortest, longest);
    }


    /**
     * Moving average from the current unit time, back for the given days
     */
    float movingAverage(int days){
        LocalDateTime t2 = unit.getDateTimeLDT();
        LocalDateTime t1 = t2.minusDays(days);
        float avg = indexUnitService.getMovingAverage(index, t1, t2);
        return avg;
    }

    @Override
    public StrategyHandler getStrategyHandler() {
        return millis -> {
            cpuPauseMs=millis;
        };
    }

    class TwoIntegers{
        public TwoIntegers(int i1, int i2) {
            this.i1 = i1;
            this.i2 = i2;
        }
        private int i1;
        private int i2;

        public int getI1() {
            return i1;
        }

        public int getI2() {
            return i2;
        }
    }

    public MarketIndex getIndex(){
        return index;
    }

}
