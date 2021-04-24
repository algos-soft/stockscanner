package com.algos.stockscanner.data.service;

import com.algos.stockscanner.beans.Utils;
import com.algos.stockscanner.data.entity.Generator;
import com.algos.stockscanner.data.entity.MarketIndex;
import com.algos.stockscanner.data.entity.Simulation;
import com.algos.stockscanner.utils.Du;
import com.algos.stockscanner.views.simulations.SimulationModel;
import com.algos.stockscanner.views.simulations.SimulationsView;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.data.provider.QuerySortOrder;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.vaadin.artur.helpers.CrudService;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class SimulationService extends CrudService<Simulation, Integer> {

    @Autowired
    private Utils utils;

    private SimulationRepository repository;

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
        Simulation entity = new Simulation();
        entity.setGenerator(generator);
        return (int) repository.count(Example.of(entity));
    }


    @Override
    protected SimulationRepository getRepository() {
        return repository;
    }


    /**
     * Copy data from Entity to View Model
     */
    private void entityToModel(Simulation entity, SimulationModel model) {
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
        String name = "Simulations";
        Sheet sheet = wb.createSheet(name);

        // create header row
        row = sheet.createRow(rowCount);

        // style for header cells
        CellStyle headerStyle = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setFont(font);

        // populate header row
        populateExcelHeaderRow(row, headerStyle);

        rowCount++;
        for(SimulationModel simulation : simulations){
            row = sheet.createRow(rowCount);
            populateExcelRow(row, simulation);
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
            e.printStackTrace();
        }

        return bos.toByteArray();

    }

    private void populateExcelHeaderRow(Row row, CellStyle style) {

        Cell cell;
        int idx=0;
        createCell(row, idx++, SimulationsView.H_NUMGEN, style);
        createCell(row, idx++, SimulationsView.H_SYMBOL, style);
        createCell(row, idx++, SimulationsView.H_START, style);
        createCell(row, idx++, SimulationsView.H_END, style);
        createCell(row, idx++, SimulationsView.H_INITIAL_AMT, style);
        createCell(row, idx++, SimulationsView.H_AMPLITUDE, style);
        createCell(row, idx++, SimulationsView.H_DAYS_BACK, style);
        createCell(row, idx++, SimulationsView.H_TERMINATION_REASON, style);
        createCell(row, idx++, SimulationsView.H_PL, style);
        createCell(row, idx++, SimulationsView.H_PL_PERCENT, style);
        createCell(row, idx++, SimulationsView.H_SPREAD, style);
        createCell(row, idx++, SimulationsView.H_COMMISSION, style);
        createCell(row, idx++, SimulationsView.H_POINTS_SCANNED, style);
        createCell(row, idx++, SimulationsView.H_NUM_POSITIONS_OPENED, style);
        createCell(row, idx++, SimulationsView.H_POINTS_IN_OPEN, style);
        createCell(row, idx++, SimulationsView.H_POINTS_IN_CLOSE, style);
        createCell(row, idx++, SimulationsView.H_MIN_SERIES_OPEN, style);
        createCell(row, idx++, SimulationsView.H_MAX_SERIES_OPEN, style);
    }

    private Cell createCell(Row row,  int idx, String value, CellStyle style){
        Cell cell = row.createCell(idx++);
        cell.setCellValue(value);
        cell.setCellStyle(style);
        return cell;
    }

    private void populateExcelRow(Row row, SimulationModel simulation) {
        Cell cell;
        cell = row.createCell(0);
        cell.setCellValue(simulation.getNumGenerator());
        cell = row.createCell(1);
        cell.setCellValue(simulation.getSymbol());
        cell = row.createCell(2);
        cell.setCellValue(simulation.getStartTs());
    }


}
