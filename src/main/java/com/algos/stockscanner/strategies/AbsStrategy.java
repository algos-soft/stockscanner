package com.algos.stockscanner.strategies;

import com.algos.stockscanner.data.entity.*;
import com.algos.stockscanner.data.enums.Terminations;
import com.algos.stockscanner.data.service.GeneratorService;
import com.algos.stockscanner.data.service.IndexUnitService;
import com.algos.stockscanner.data.service.SimulationService;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
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


    @Autowired
    IndexUnitService indexUnitService;

    @Autowired
    SimulationService simulationService;

    @Autowired
    GeneratorService generatorService;

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
        simulation.setTerminationCode(termination.getCode());

        return simulation;

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

}
