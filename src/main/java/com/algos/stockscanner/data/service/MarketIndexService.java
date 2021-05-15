package com.algos.stockscanner.data.service;

import com.algos.stockscanner.Application;
import com.algos.stockscanner.beans.ExportUtils;
import com.algos.stockscanner.beans.Utils;
import com.algos.stockscanner.data.entity.MarketIndex;
import com.algos.stockscanner.data.entity.Simulation;
import com.algos.stockscanner.enums.FrequencyTypes;
import com.algos.stockscanner.enums.IndexCategories;
import com.algos.stockscanner.services.IndexEntry;
import com.algos.stockscanner.utils.Du;
import com.algos.stockscanner.views.indexes.IndexModel;
import com.algos.stockscanner.views.simulations.SimulationModel;
import com.algos.stockscanner.views.simulations.SimulationsView;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import com.vaadin.flow.data.provider.QuerySortOrder;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.vaadin.artur.helpers.CrudService;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class MarketIndexService extends CrudService<MarketIndex, Integer> {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private Utils utils;

    @Autowired
    private ExportUtils eUtils;

    @Autowired
    private IndexUnitService indexUnitService;

    private MarketIndexRepository repository;

    public MarketIndexService(@Autowired MarketIndexRepository repository) {
        this.repository = repository;
    }


    public List<MarketIndex> findBySymbol (String symbol){
        return repository.findBySymbol(symbol);
    }

    public MarketIndex findUniqueBySymbol (String symbol) throws Exception {
        List<MarketIndex> list = repository.findBySymbol(symbol);
        if(list.size()!=1){
            if(list.size()==0){
                throw new Exception("Symbol "+symbol+" not found in database.");
            }else{
                throw new Exception("Multiple instances ("+list.size()+") of Symbol "+symbol+" present or in database.");
            }
        }
        return list.get(0);
    }


    public List<MarketIndex> fetch(int offset, int limit, Example<MarketIndex> example, List<QuerySortOrder> orders) {

        Sort sort = utils.buildSort(orders);

        Pageable pageable = new OffsetBasedPageRequest(offset, limit, sort);

        Page<MarketIndex> page;
        if (example != null) {
            page = repository.findAll(example, pageable);
        } else {
            page = repository.findAll(pageable);
        }

        return page.toList();
    }

    public int count(Example<MarketIndex> example) {
        return (int)repository.count(example);
    }

    public List<MarketIndex> findAll ()  {
        return repository.findAll();
    }

    public List<MarketIndex> findAllOrderBySymbol(){
        return repository.findAllAndSort(Sort.by("symbol"));
    }

    public Page<MarketIndex> findAllOrderBySymbol(Pageable pageable){
        return repository.findAllOrderBySymbol(pageable);
    }

    public Page<MarketIndex> findAllWithFilterOrderBySymbol(Pageable pageable, String filter){
        return repository.findAllWithFilterOrderBySymbol(pageable, filter, filter);
    }



    public int countDataPoints(MarketIndex index){
        return indexUnitService.countBy(index);
    }


    @Override
    protected MarketIndexRepository getRepository() {
        return repository;
    }

    /**
     * Copy data from Entity to Model
     */
    public void entityToModel(MarketIndex entity, IndexModel model){
        model.setId(entity.getId());
        model.setSymbol(entity.getSymbol());
        model.setName(entity.getName());

        String categoryCode=entity.getCategory();
        Optional<IndexCategories> oCategory= IndexCategories.getItem(categoryCode);
        if(oCategory.isPresent()){
            model.setCategory(oCategory.get());
        }

        model.setExchange(entity.getExchange());
        model.setCountry(entity.getCountry());
        model.setSector(entity.getSector());
        model.setIndustry(entity.getIndustry());
        model.setMarketCap(utils.toPrimitive(entity.getMarketCap()));
        model.setEbitda(utils.toPrimitive(entity.getEbitda()));

        model.setImageData(entity.getImage());
        model.setSymbol(entity.getSymbol());

        model.setSpreadPercent(utils.toPrimitive(entity.getSpreadPercent()));
        model.setOvnBuyDay(utils.toPrimitive(entity.getOvnBuyDay()));
        model.setOvnBuyWe(utils.toPrimitive(entity.getOvnBuyWe()));
        model.setOvnSellDay(utils.toPrimitive(entity.getOvnSellDay()));
        model.setOvnSellWe(utils.toPrimitive(entity.getOvnSellWe()));

        model.setUnitsFrom(Du.toLocalDate(entity.getUnitsFrom()));
        model.setUnitsTo(Du.toLocalDate(entity.getUnitsTo()));
        model.setNumUnits(utils.toPrimitive(entity.getNumUnits()));

        String frequencyCode=entity.getUnitFrequency();
        Optional<FrequencyTypes> oFrequency= FrequencyTypes.getItem(frequencyCode);
        if(oFrequency.isPresent()){
            model.setUnitFrequency(oFrequency.get());
        }

        model.setFundamentalUpdateTs(Du.toLocalDateTime(entity.getFundamentalUpdateTs()));
        model.setPricesUpdateTs(Du.toLocalDateTime(entity.getPricesUpdateTs()));

    }


    /**
     * Update entity from model
     */
    public void modelToEntity(IndexModel model, MarketIndex entity) {
        entity.setImage(model.getImageData());
        entity.setSymbol(model.getSymbol());
        entity.setName(model.getName());

        IndexCategories category = model.getCategory();
        if (category != null) {
            entity.setCategory(category.getCode());
        }

        entity.setExchange(model.getExchange());
        entity.setCountry(model.getCountry());
        entity.setSector(model.getSector());
        entity.setIndustry(model.getIndustry());
        entity.setMarketCap(model.getMarketCap());
        entity.setEbitda(model.getEbitda());

        entity.setSpreadPercent(model.getSpreadPercent());
        entity.setOvnBuyDay(model.getOvnBuyDay());
        entity.setOvnBuyWe(model.getOvnBuyWe());
        entity.setOvnSellDay(model.getOvnSellDay());
        entity.setOvnSellWe(model.getOvnSellWe());
    }


    public List<IndexModel> entitiesToModels(List<MarketIndex> entities) {
        List<IndexModel> list = new ArrayList<>();
        for (MarketIndex entity : entities) {
            IndexModel model = new IndexModel();
            entityToModel(entity, model);
            list.add(model);
        }
        return list;
    }


    /**
     * Return the list of all the available symbols
     */
    public List<IndexEntry> loadAllAvailableSymbols(){

        String filename= Application.ALL_AVAILABLE_SYMBOLS;
        File indexesFile = new File(filename);
        if(!indexesFile.exists()){
            log.error("File "+filename+" not found.");
            return null;
        }

        // parse the file into a list of objects
        List<IndexEntry> entries = new ArrayList<>();
        try {
            FileReader fileReader = new FileReader(indexesFile);
            CSVReader reader = new CSVReader(fileReader);
            List<String[]> list = reader.readAll();
            for(String[] element : list){
                if(element.length>=2){
                    String symbol = element[0].trim();
                    String type = element[1].trim();
                    entries.add(new IndexEntry(symbol, type));
                }else{
                    log.warn("Invalid element "+element+" in "+filename);
                }
            }
        } catch (IOException | CsvException e ) {
            log.error("could not parse the file "+indexesFile.toString(), e);
        }

        return entries;

    }


    /**
     * Find the entities corresponding to a list of symbols
     */
    public List<MarketIndex> findBySymbolList(List<String> symbols) throws Exception {
        List<MarketIndex> indexes = new ArrayList<>();
        for(String symbol : symbols){
            MarketIndex entity=findUniqueBySymbol(symbol);
            indexes.add(entity);
        }
        return indexes;
    }


    public byte[] exportExcel(Example<MarketIndex> filter, List<QuerySortOrder> orders) {

        List<MarketIndex> indexes = fetch(0, count(filter), filter, orders);
        if(indexes.size()==0){
            return null;
        }

        int rowCount = 0;
        Row row;
        Workbook wb = new HSSFWorkbook();
        String name = "Indexes";
        Sheet sheet = wb.createSheet(name);

        // create header row
        row = sheet.createRow(rowCount);


        // populate header row
        populateExcelHeaderRow(wb, row);

        rowCount++;
        for(MarketIndex index : indexes){
            row = sheet.createRow(rowCount);
            populateExcelRow(wb, row, index);
            rowCount++;
        }

        // at the end autosize each column
        int numColumns = sheet.getRow(0).getPhysicalNumberOfCells();
        for(int i=0; i<numColumns; i++){
            sheet.autoSizeColumn(i);
        }

//        // write the workbook to a byte array to return
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            wb.write(bos);
        } catch (IOException e) {
            log.error("can't serialize excel workbook", e);
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
        eUtils.createCell(wb, row, idx++, SimulationsView.H_NUMGEN, style);
        eUtils.createCell(wb, row, idx++, SimulationsView.H_SYMBOL, style);
    }


    /**
     * Populates a row of the Excel with the cells created from a Simulation item
     */
    private void populateExcelRow(Workbook wb, Row row, MarketIndex index) {
        int idx=0;
//        eUtils.createCell(wb, row, idx++, simulation.getNumGenerator(), wb.createCellStyle());
//        eUtils.createCell(wb, row, idx++, simulation.getSymbol(), wb.createCellStyle());
//        eUtils.createCell(wb, row, idx++, simulation.getStartTs(), wb.createCellStyle());
    }





}
