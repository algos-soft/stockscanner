package com.algos.stockscanner.strategies;

import com.algos.stockscanner.data.entity.*;
import com.algos.stockscanner.data.enums.ActionTypes;
import com.algos.stockscanner.data.enums.Actions;
import com.algos.stockscanner.data.enums.Reasons;
import com.algos.stockscanner.data.enums.Terminations;
import com.algos.stockscanner.data.service.GeneratorService;
import com.algos.stockscanner.data.service.IndexUnitService;
import com.algos.stockscanner.data.service.SimulationItemService;
import com.algos.stockscanner.data.service.SimulationService;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public abstract class AbsStrategy implements Strategy {

    private static final int PAGE_SIZE = 100;      // max units per page

    StrategyParams params;

    boolean abort=false;


    Simulation simulation;

    // current page of units scanned
    List<IndexUnit> unitsPage=new ArrayList<>();

    // Index of the current unit in the units page, 0 for the first
    int unitIndex=0;

    // Currently scanned unit
    IndexUnit unit;

    // the reason why has terminated
    Terminations termination;

    boolean posOpen; //a position is open

    ActionTypes posType; // if open for buy or sell

    float openPrice;    // opened at this price


    @Autowired
    IndexUnitService indexUnitService;

    @Autowired
    SimulationService simulationService;

    @Autowired
    GeneratorService generatorService;

    @Autowired
    SimulationItemService simulationItemService;


    public AbsStrategy(StrategyParams params) {
        this.params=params;
    }

    public Simulation execute() throws Exception {

        //generator = generatorService.get(params.getGeneratorId()).get();

        // create a new Simulation
        simulation = new Simulation();
        simulation.setIndex(params.getIndex());
        simulation.setStartTsLDT(params.getStartDate().atStartOfDay());
        simulation.setInitialAmount(params.getInitialAmount());
        simulation.setLeverage(params.getLeverage());
        simulation.setSl(params.getSl());
        simulation.setTp(params.getTp());
        simulation.setAmplitude(params.getAmplitude());
        simulation.setDaysLookback(params.getDaysLookback());

        // you can exit this cycle only with a termination code assigned
        do {

            if(abort){
                termination=Terminations.ABORTED_BY_USER;
                break;
            }

            if(ensureUnitsAvailable()){

                unit = unitsPage.get(unitIndex);

                Terminations term = isFinished();
                if(term!=null){
                    termination=term;
                    break;
                }

                processUnit();

            }else{
                termination =Terminations.NO_MORE_DATA;
                break;
            }

            unitIndex++;

        } while (true);

        // consolidate data in the simulation
        consolidate();

        return simulation;

    }




    /***
     * consolidate data in the simulation
     */
    private void consolidate(){
        simulation.setTerminationCode(termination.getCode());
        if(unit!=null){
            simulation.setEndTsLDT(unit.getDateTimeLDT());
        }else{
            simulation.setEndTsLDT(simulation.getStartTsLDT());
        }
    }




    /**
     * Ensure that we still have units available in the current page.
     * If not, load another page.
     */
    private boolean ensureUnitsAvailable(){

        MarketIndex index = params.getIndex();

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
                LocalDate date = params.getStartDate();
                int firstId = indexUnitService.findFirstIdOf(index, date);
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
    public void processUnit() throws Exception {

        Thread.sleep(5);
        //System.out.println(unitIndex + " " + unit.getId() + " " + unit.getDateTime());

        Decision decision = takeDecision();
        Actions action = decision.getAction();
        Reasons reason = decision.getReason();

        System.out.println(decision);

        switch (action) {

            case OPEN:

                if(posOpen){
                    throw new Exception("Position is already open, you can't open it again");
                }

                switch (decision.getActionType()){
                    case BUY:
                        openBuy(reason);
                        break;
                    case SELL:
                        openSell(reason);
                        break;
                }

                break;

            case CLOSE:

                if(!posOpen){
                    throw new Exception("Position is already closed, you can't close it again");
                }
                closePosition(decision.getReason());
                posOpen =false;
                break;

            case STAY:
                break;
        }

    }




    /**
     * Take a decision based on the current state
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


    private void openBuy(Reasons reason){
        SimulationItem item = new SimulationItem();
        item.setSimulation(simulation);
        item.setAction(ActionTypes.BUY.getCode());
        item.setTimestamp(unit.getDateTime());
        item.setReason(reason.getCode());
        simulation.getSimulationItems().add(item);

        posOpen =true;
        posType=ActionTypes.BUY;
        openPrice = unit.getClose();

    }

    private void openSell(Reasons reason){
        SimulationItem item = new SimulationItem();
        item.setSimulation(simulation);
        item.setAction(ActionTypes.SELL.getCode());
        item.setTimestamp(unit.getDateTime());
        item.setReason(reason.getCode());
        simulation.getSimulationItems().add(item);

        posOpen =true;
        posType=ActionTypes.SELL;
        openPrice = unit.getClose();

    }

    private void closePosition(Reasons reason){


        posOpen =false;
        posType=null;
        openPrice=0;
    }


    /**
     * delta percent between the first and the seconb number
     */
    float deltaPercent(float first, float second){
        if(first==0){
            return 0;
        }
        float deltaPercent = (second - first) * 100 / first;
        return deltaPercent;
    }

    /**
     * Average price in the backlook period starting from the current unit time
     */
    float avgBackPrice(){
        LocalDateTime t2 = unit.getDateTimeLDT();
        LocalDateTime t1 = t2.minusDays(simulation.getDaysLookback());
        float avg = indexUnitService.getAveragePrice(simulation.getIndex(), t1, t2);
        return avg;
    }

}
