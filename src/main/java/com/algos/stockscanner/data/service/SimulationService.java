package com.algos.stockscanner.data.service;

import com.algos.stockscanner.beans.ExportHelper;
import com.algos.stockscanner.beans.ExportUtils;
import com.algos.stockscanner.beans.Utils;
import com.algos.stockscanner.data.entity.Generator;
import com.algos.stockscanner.data.entity.MarketIndex;
import com.algos.stockscanner.data.entity.Simulation;
import com.algos.stockscanner.views.simulations.SimulationModel;
import com.algos.stockscanner.views.simulations.SimulationsView;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.data.provider.QuerySortOrder;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.vaadin.artur.helpers.CrudService;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class SimulationService extends CrudService<Simulation, Integer> {

    private static final Logger log = LoggerFactory.getLogger(SimulationService.class);

    @Autowired
    private Utils utils;

    private SimulationRepository repository;

    @Autowired
    private ApplicationContext context;


    public SimulationService(@Autowired SimulationRepository repository) {
        this.repository = repository;
    }


    public List<SimulationModel> fetch(int offset, int limit, Example<Simulation> example, List<QuerySortOrder> orders) {

        Sort sort = utils.buildSort(orders);

        Pageable pageable = new OffsetBasedPageRequest(offset, limit, sort);

        Page<Simulation> page;
        if (example != null) {
            page = repository.findAll(example, pageable);
        } else {
            page = repository.findAll(pageable);
        }

        List<SimulationModel> list = new ArrayList<>();
        for (Simulation entity : page.toList()) {
            SimulationModel model = new SimulationModel();
            entityToModel(entity, model);
            list.add(model);
        }

        return list;
    }




//    private Sort buildSort(List<QuerySortOrder> orders){
//
//        List<Sort.Order> sortOrders = new ArrayList<>();
//
//        for(QuerySortOrder order : orders){
//
//            SortDirection sortDirection = order.getDirection();
//            String sortProperty = order.getSorted();
//
//            Sort.Direction sDirection=null;
//            switch (sortDirection){
//                case ASCENDING:
//                    sDirection=Sort.Direction.ASC;
//                    break;
//                case DESCENDING:
//                    sDirection=Sort.Direction.DESC;
//                    break;
//            }
//
//            sortOrders.add(new Sort.Order(sDirection, sortProperty));
//
//        }
//
//        return Sort.by(sortOrders);
//    }

    public int count(Example<Simulation> example) {
        return (int) repository.count(example);
    }

    public int count() {
        return (int) repository.count();
    }

    public int countBy(Generator generator) {
        return repository.countByGenerator(generator);
    }

    public void deleteBy(Generator generator) {
        for (Iterator<Simulation> iterator = generator.getSimulations().iterator(); iterator.hasNext(); ) {
            Simulation simulation = iterator.next();
            delete(simulation.getId());
            iterator.remove();
        }
    }


    @Override
    protected SimulationRepository getRepository() {
        return repository;
    }


    /**
     * Copy data from Entity to View Model
     */
    public void entityToModel(Simulation entity, SimulationModel model) {
        model.setId(utils.toPrimitive(entity.getId()));

        if (entity.getIndex() != null) {
            model.setId(entity.getId());
            Generator gen = entity.getGenerator();
            if (gen != null) {
                model.setNumGenerator(utils.toPrimitive(gen.getNumber()));
            }
            MarketIndex index = entity.getIndex();
            if (index != null) {
                model.setSymbol(index.getSymbol());
            }
            model.setStartTs(entity.getStartTsLD());
            model.setEndTs(entity.getEndTsLD());
            model.setInitialAmount(utils.toPrimitive(entity.getInitialAmount()));
            model.setAmplitude(utils.toPrimitive(entity.getAmplitude()));
            model.setDaysLookback(utils.toPrimitive(entity.getDaysLookback()));
            model.setTerminationCode(entity.getTerminationCode());
            model.setTotSpread(utils.toPrimitive(entity.getTotSpread()));
            model.setTotCommission(utils.toPrimitive(entity.getTotCommission()));
            model.setPl(utils.toPrimitive(entity.getPl()));
            model.setPlPercent(utils.toPrimitive(entity.getPlPercent()));
            model.setNumPointsScanned(utils.toPrimitive(entity.getNumPointsTotal()));
            model.setNumOpenings(utils.toPrimitive(entity.getNumOpenings()));
            model.setNumPointsHold(utils.toPrimitive(entity.getNumPointsOpen()));
            model.setNumPointsWait(utils.toPrimitive(entity.getNumPointsClosed()));
            model.setMinPointsHold(utils.toPrimitive(entity.getShortestPeriodOpen()));
            model.setMaxPointsHold(utils.toPrimitive(entity.getLongestPeriodOpen()));

            byte[] imageData = entity.getIndex().getImage();
            Image image = utils.byteArrayToImage(imageData);
            model.setImage(image);

        }

    }


    public byte[] exportExcel(Example<Simulation> filter, List<QuerySortOrder> orders) {

        List<SimulationModel> simulations = fetch(0, count(filter), filter, orders);
        if(simulations.size()==0){
            return null;
        }

        int rowCount = 0;
        Row row;
        Workbook wb = new HSSFWorkbook();
        ExportHelper helper = context.getBean(ExportHelper.class, wb);

        String name = "Simulations";
        Sheet sheet = wb.createSheet(name);

        // create header row
        row = sheet.createRow(rowCount);

        // populate header row
        populateExcelHeaderRow(helper, row);

        rowCount++;
        for(SimulationModel simulation : simulations){
            row = sheet.createRow(rowCount);
            populateExcelRow(helper, row, simulation);
            rowCount++;
        }

        // at the end autosize each column
        int numColumns = sheet.getRow(0).getPhysicalNumberOfCells();
        for(int i=0; i<numColumns; i++){
            sheet.autoSizeColumn(i);
        }

        // write the workbook to a byte array to return
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            wb.write(bos);
        } catch (IOException e) {
            log.error("can't serialize excel workbook", e);
            e.printStackTrace();
        }

        return bos.toByteArray();

    }

    private void populateExcelHeaderRow(ExportHelper helper, Row row) {

        int idx=0;

        CellStyle style=helper.getStyle(ExportHelper.Styles.HEADER);
        helper.createCell( row, idx++, SimulationsView.H_NUMGEN, style);
        helper.createCell( row, idx++, SimulationsView.H_SYMBOL, style);
        helper.createCell( row, idx++, SimulationsView.H_START, style);
        helper.createCell( row, idx++, SimulationsView.H_END, style);
        helper.createCell(row, idx++, SimulationsView.H_INITIAL_AMT, style);
        helper.createCell( row, idx++, SimulationsView.H_AMPLITUDE, style);
        helper.createCell( row, idx++, SimulationsView.H_DAYS_BACK, style);
        helper.createCell( row, idx++, SimulationsView.H_TERMINATION_REASON, style);
        helper.createCell( row, idx++, SimulationsView.H_PL, style);
        helper.createCell( row, idx++, SimulationsView.H_PL_PERCENT, style);
        helper.createCell( row, idx++, SimulationsView.H_SPREAD, style);
        helper.createCell( row, idx++, SimulationsView.H_COMMISSION, style);
        helper.createCell( row, idx++, SimulationsView.H_POINTS_SCANNED, style);
        helper.createCell( row, idx++, SimulationsView.H_NUM_POSITIONS_OPENED, style);
        helper.createCell( row, idx++, SimulationsView.H_POINTS_IN_OPEN, style);
        helper.createCell( row, idx++, SimulationsView.H_POINTS_IN_CLOSE, style);
        helper.createCell(row, idx++, SimulationsView.H_MIN_SERIES_OPEN, style);
        helper.createCell( row, idx++, SimulationsView.H_MAX_SERIES_OPEN, style);
    }


    /**
     * Populates a row of the Excel with the cells created from a Simulation item
     */
    private void populateExcelRow(ExportHelper helper, Row row, SimulationModel simulation) {
        int idx=0;
        helper.createCell( row, idx++, simulation.getNumGenerator());
        helper.createCell( row, idx++, simulation.getSymbol());
        helper.createCell( row, idx++, simulation.getStartTs());
        helper.createCell( row, idx++, simulation.getEndTs());
        helper.createCell( row, idx++, simulation.getInitialAmount());
        helper.createCell( row, idx++, (int)simulation.getAmplitude());
        helper.createCell(row, idx++, simulation.getDaysLookback());
        helper.createCell( row, idx++, simulation.getTerminationCode());
        helper.createCell( row, idx++, simulation.getPl());
        helper.createCell( row, idx++, simulation.getPlPercent());
        helper.createCell( row, idx++, simulation.getTotSpread());
        helper.createCell( row, idx++, simulation.getTotCommission());
        helper.createCell( row, idx++, simulation.getNumPointsScanned());
        helper.createCell( row, idx++, simulation.getNumOpenings());
        helper.createCell( row, idx++, simulation.getNumPointsHold());
        helper.createCell( row, idx++, simulation.getNumPointsWait());
        helper.createCell( row, idx++, simulation.getMinPointsHold());
        helper.createCell( row, idx++, simulation.getMaxPointsHold());
    }


}
