package com.algos.stockscanner.data.service;

import com.algos.stockscanner.Application;
import com.algos.stockscanner.beans.ExportHelper;
import com.algos.stockscanner.beans.Utils;
import com.algos.stockscanner.data.entity.MarketIndex;
import com.algos.stockscanner.enums.FrequencyTypes;
import com.algos.stockscanner.enums.IndexCategories;
import com.algos.stockscanner.services.IndexEntry;
import com.algos.stockscanner.utils.Du;
import com.algos.stockscanner.views.indexes.IndexFilter;
import com.algos.stockscanner.views.indexes.IndexModel;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.QuerySortOrder;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.vaadin.artur.helpers.CrudService;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

@Service
public class MarketIndexService extends CrudService<MarketIndex, Integer> {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    // export file headers
    public static final String H_SYMBOL = "symbol";
    public static final String H_NAME = "name";
    public static final String H_CATEGORY = "category";
    public static final String H_EXCHANGE = "exchange";
    public static final String H_COUNTRY = "country";
    public static final String H_SECTOR = "sector";
    public static final String H_INDUSTRY = "industry";
    public static final String H_MARKETCAP = "marketCap";
    public static final String H_EBITDA = "ebitda";

    @Autowired
    private Utils utils;

    @Autowired
    private IndexUnitService indexUnitService;

    private MarketIndexRepository repository;

    @Autowired
    private ApplicationContext context;

    public MarketIndexService(@Autowired MarketIndexRepository repository) {
        this.repository = repository;
    }


    public List<MarketIndex> findBySymbol(String symbol) {
        return repository.findBySymbol(symbol);
    }

    public MarketIndex findUniqueBySymbol(String symbol) throws Exception {
        List<MarketIndex> list = repository.findBySymbol(symbol);
        if (list.size() != 1) {
            if (list.size() == 0) {
                throw new Exception("Symbol " + symbol + " not found in database.");
            } else {
                throw new Exception("Multiple instances (" + list.size() + ") of Symbol " + symbol + " present or in database.");
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
        return (int) repository.count(example);
    }

    public List<MarketIndex> fetch(int offset, int limit, IndexFilter f, List<QuerySortOrder> orders) {

        Sort sort = utils.buildSort(orders);
        Pageable pageable = new OffsetBasedPageRequest(offset, limit, sort);

        Page<MarketIndex> page;
        if (f != null) {
            page = repository.findAllWithFilterOrderBySymbol(pageable, f.symbol, f.name, f.exchange, f.country, f.sector, f.industry, f.marketCapFrom, f. marketCapTo, f.ebitdaFrom, f.ebitdaTo);
        } else {
            page = repository.findAll(pageable);
        }

        return page.toList();
    }

    public int count(IndexFilter f) {
        int count;
        if (f != null) {
            count = (int)repository.count(f.symbol, f.name, f.exchange, f.country, f.sector, f.industry, f.marketCapFrom, f. marketCapTo, f.ebitdaFrom, f.ebitdaTo);
        } else {
            count = (int)repository.count();
        }

        return count;
    }


    public List<MarketIndex> fetch(int offset, int limit, Set<MarketIndex> indexes, List<QuerySortOrder> orders) {
        Sort sort = utils.buildSort(orders);
        Pageable pageable = new OffsetBasedPageRequest(offset, limit, sort);
        Page<MarketIndex> page = repository.findAllInSet(pageable, indexes);
        return page.toList();
    }




    public List<MarketIndex> findAll() {
        return repository.findAll();
    }

    public List<MarketIndex> findAllOrderBySymbol() {
        return repository.findAllAndSort(Sort.by("symbol"));
    }

    public Page<MarketIndex> findAllOrderBySymbol(Pageable pageable) {
        return repository.findAllOrderBySymbol(pageable);
    }

    public Page<MarketIndex> findAllWithFilterOrderBySymbol(Pageable pageable, String filter) {
        return repository.findAllWithFilterOrderBySymbol(pageable, filter, filter);
    }

    public List<MarketIndex> findAllOrderByUnitsToLimit(int limit){
        Pageable pageable = PageRequest.of(0, limit);
        Page page = repository.findAllOrderByUnitsTo(pageable);
        return page.toList();
    }

    public int countDataPoints(MarketIndex index) {
        return indexUnitService.countBy(index);
    }


    @Override
    public void delete(Integer id) {
        MarketIndex index = get(id).get();
        super.delete(id);
        log.info("Market index deleted: "+index);
    }

    @Override
    protected MarketIndexRepository getRepository() {
        return repository;
    }

    /**
     * Copy data from Entity to Model
     */
    public void entityToModel(MarketIndex entity, IndexModel model) {
        model.setId(entity.getId());
        model.setSymbol(entity.getSymbol());
        model.setName(entity.getName());

        String categoryCode = entity.getCategory();
        Optional<IndexCategories> oCategory = IndexCategories.getItem(categoryCode);
        if (oCategory.isPresent()) {
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

        String frequencyCode = entity.getUnitFrequency();
        Optional<FrequencyTypes> oFrequency = FrequencyTypes.getItem(frequencyCode);
        if (oFrequency.isPresent()) {
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
    public List<IndexEntry> loadAllAvailableSymbols() {

        String filename = Application.ALL_AVAILABLE_SYMBOLS;
        File indexesFile = new File(filename);
        if (!indexesFile.exists()) {
            log.error("File " + filename + " not found.");
            return null;
        }

        // parse the file into a list of objects
        List<IndexEntry> entries = new ArrayList<>();
        try {
            FileReader fileReader = new FileReader(indexesFile);
            CSVReader reader = new CSVReader(fileReader);
            List<String[]> list = reader.readAll();
            for (String[] element : list) {
                if (element.length >= 2) {
                    String symbol = element[0].trim();
                    String type = element[1].trim();
                    entries.add(new IndexEntry(symbol, type));
                } else {
                    log.warn("Invalid element " + element + " in " + filename);
                }
            }
        } catch (IOException | CsvException e) {
            log.error("could not parse the file " + indexesFile.toString(), e);
        }

        return entries;

    }


    /**
     * Find the entities corresponding to a list of symbols
     */
    public List<MarketIndex> findBySymbolList(List<String> symbols) throws Exception {
        List<MarketIndex> indexes = new ArrayList<>();
        for (String symbol : symbols) {
            MarketIndex entity = findUniqueBySymbol(symbol);
            indexes.add(entity);
        }
        return indexes;
    }


    public byte[] exportExcel(DataProvider dataProvider) {

        Query query = new Query();
        Stream<IndexModel> indexStream = dataProvider.fetch(query);
        Iterator<IndexModel> iterator = indexStream.iterator();

        int rowCount = 0;
        Row row;
        Workbook wb = new HSSFWorkbook();
        ExportHelper helper = context.getBean(ExportHelper.class, wb);

        String name = "Indexes";
        Sheet sheet = wb.createSheet(name);


        // create header row
        row = sheet.createRow(rowCount);

        // populate header row
        populateExcelHeaderRow(helper, row);

        rowCount++;

        while (iterator.hasNext()) {
            IndexModel indexModel = iterator.next();
            row = sheet.createRow(rowCount);
            populateExcelRow(helper, row, indexModel);
            rowCount++;
        }

        // at the end autosize each column
        int numColumns = sheet.getRow(0).getPhysicalNumberOfCells();
        for (int i = 0; i < numColumns; i++) {
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

        int idx = 0;
        CellStyle style=helper.getStyle(ExportHelper.Styles.HEADER);
        helper.createCell(row, idx++, H_SYMBOL, style);
        helper.createCell(row, idx++, H_NAME, style);
        helper.createCell(row, idx++, H_CATEGORY, style);
        helper.createCell(row, idx++, H_EXCHANGE, style);
        helper.createCell(row, idx++, H_COUNTRY, style);
        helper.createCell(row, idx++, H_SECTOR, style);
        helper.createCell(row, idx++, H_INDUSTRY, style);
        helper.createCell(row, idx++, H_MARKETCAP, style);
        helper.createCell(row, idx++, H_EBITDA, style);

    }


    /**
     * Populates a row of the Excel with the cells created from a Simulation item
     */
    private void populateExcelRow(ExportHelper helper, Row row, IndexModel index) {
        int idx = 0;
        helper.createCell( row, idx++, index.getSymbol());
        helper.createCell( row, idx++, index.getName());
        helper.createCell( row, idx++, index.getCategory().getCode());
        helper.createCell( row, idx++, index.getExchange());
        helper.createCell( row, idx++, index.getCountry());
        helper.createCell( row, idx++, index.getSector());
        helper.createCell( row, idx++, index.getIndustry());
        helper.createCell( row, idx++, index.getMarketCap());
        helper.createCell( row, idx++, index.getEbitda());

    }


}
