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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
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
        String name = "Simulations";
        Sheet sheet = wb.createSheet(name);

        // create header row
        row = sheet.createRow(rowCount);


        // populate header row
        populateExcelHeaderRow(wb, row);

        rowCount++;
        for(SimulationModel simulation : simulations){
            row = sheet.createRow(rowCount);
            populateExcelRow(wb, row, simulation);
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

    private void populateExcelHeaderRow(Workbook wb, Row row) {

        // same style for all header cells
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setFont(font);

        int idx=0;
        createCell(wb, row, idx++, SimulationsView.H_NUMGEN, style);
        createCell(wb, row, idx++, SimulationsView.H_SYMBOL, style);
        createCell(wb, row, idx++, SimulationsView.H_START, style);
        createCell(wb, row, idx++, SimulationsView.H_END, style);
        createCell(wb, row, idx++, SimulationsView.H_INITIAL_AMT, style);
        createCell(wb, row, idx++, SimulationsView.H_AMPLITUDE, style);
        createCell(wb, row, idx++, SimulationsView.H_DAYS_BACK, style);
        createCell(wb, row, idx++, SimulationsView.H_TERMINATION_REASON, style);
        createCell(wb, row, idx++, SimulationsView.H_PL, style);
        createCell(wb, row, idx++, SimulationsView.H_PL_PERCENT, style);
        createCell(wb, row, idx++, SimulationsView.H_SPREAD, style);
        createCell(wb, row, idx++, SimulationsView.H_COMMISSION, style);
        createCell(wb, row, idx++, SimulationsView.H_POINTS_SCANNED, style);
        createCell(wb, row, idx++, SimulationsView.H_NUM_POSITIONS_OPENED, style);
        createCell(wb, row, idx++, SimulationsView.H_POINTS_IN_OPEN, style);
        createCell(wb, row, idx++, SimulationsView.H_POINTS_IN_CLOSE, style);
        createCell(wb, row, idx++, SimulationsView.H_MIN_SERIES_OPEN, style);
        createCell(wb, row, idx++, SimulationsView.H_MAX_SERIES_OPEN, style);
    }


    /**
     * Populates a row of the Excel with the cells created from a Simulation item
     */
    private void populateExcelRow(Workbook wb, Row row, SimulationModel simulation) {
        int idx=0;
        createCell(wb, row, idx++, simulation.getNumGenerator(), wb.createCellStyle());
        createCell(wb, row, idx++, simulation.getSymbol(), wb.createCellStyle());
        createCell(wb, row, idx++, simulation.getStartTs(), wb.createCellStyle());
        createCell(wb, row, idx++, simulation.getEndTs(), wb.createCellStyle());
        createCell(wb, row, idx++, simulation.getInitialAmount(), wb.createCellStyle());
        createCell(wb, row, idx++, (int)simulation.getAmplitude(), wb.createCellStyle());
        createCell(wb, row, idx++, simulation.getDaysLookback(), wb.createCellStyle());
        createCell(wb, row, idx++, simulation.getTerminationCode(), wb.createCellStyle());
        createCell(wb, row, idx++, simulation.getPl(), wb.createCellStyle());
        createCell(wb, row, idx++, simulation.getPlPercent(), wb.createCellStyle());
        createCell(wb, row, idx++, simulation.getTotSpread(), wb.createCellStyle());
        createCell(wb, row, idx++, simulation.getTotCommission(), wb.createCellStyle());
        createCell(wb, row, idx++, simulation.getNumPointsScanned(), wb.createCellStyle());
        createCell(wb, row, idx++, simulation.getNumOpenings(), wb.createCellStyle());
        createCell(wb, row, idx++, simulation.getNumPointsHold(), wb.createCellStyle());
        createCell(wb, row, idx++, simulation.getNumPointsWait(), wb.createCellStyle());
        createCell(wb, row, idx++, simulation.getMinPointsHold(), wb.createCellStyle());
        createCell(wb, row, idx++, simulation.getMaxPointsHold(), wb.createCellStyle());
    }


    /**
     * Create a String cell
     */
    private Cell createCell(Workbook wb, Row row,  int idx, String value, CellStyle style){
        Cell cell = createCell(row, idx, style);
        if(value!=null){
            cell.setCellValue(value);
        }
        return cell;
    }

    /**
     * Create a Number (float) cell
     */
    private Cell createCell(Workbook wb, Row row, int idx, float value, CellStyle style){
        Cell cell = createCell(row, idx, style);
        style.setDataFormat(wb.getCreationHelper().createDataFormat().getFormat("#,##0.00;-#,##0.00;@"));
        cell.setCellValue(value);
        return cell;
    }

    /**
     * Create a Integer cell
     */
    private Cell createCell(Workbook wb, Row row, int idx, int value, CellStyle style){
        Cell cell = createCell(row, idx, style);
        style.setDataFormat(wb.getCreationHelper().createDataFormat().getFormat("#,###;#,###;@"));
        cell.setCellValue(value);
        return cell;
    }

    /**
     * Create a Date cell
     */
    private Cell createCell(Workbook wb, Row row, int idx, LocalDate value, CellStyle style){
        Cell cell = createCell(row, idx, style);
        style.setDataFormat(wb.getCreationHelper().createDataFormat().getFormat("dd/mm/yyyy"));
        if(value!=null){
            cell.setCellValue(value);
        }
        return cell;
    }


    /**
     * Create a single cell in a row with a Style
     */
    private Cell createCell(Row row,  int idx, CellStyle style){
        Cell cell = createCell(row, idx++);
        if(style!=null){
            cell.setCellStyle(style);
        }
        return cell;
    }

    /**
     * Create a single cell in a row
     */
    private Cell createCell(Row row,  int idx){
        return row.createCell(idx++);
    }


}
