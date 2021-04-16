package com.algos.stockscanner.data.service;

import com.algos.stockscanner.beans.Utils;
import com.algos.stockscanner.data.entity.IndexUnit;
import com.algos.stockscanner.data.entity.MarketIndex;
import com.algos.stockscanner.utils.Du;
import com.vaadin.flow.data.provider.QuerySortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.vaadin.artur.helpers.CrudService;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class IndexUnitService extends CrudService<IndexUnit, Integer> {

    private IndexUnitRepository repository;

    @Autowired
    private Utils utils;


    public IndexUnitService(@Autowired IndexUnitRepository repository) {
        this.repository = repository;
    }


    public List<IndexUnit> fetch(int offset, int limit, Example<IndexUnit> example, List<QuerySortOrder> orders) {

        Sort sort = utils.buildSort(orders);

        Pageable pageable = new OffsetBasedPageRequest(offset, limit, sort);

        Page<IndexUnit> page;
        if (example != null) {
            page = repository.findAll(example, pageable);
        } else {
            page = repository.findAll(pageable);
        }

        return page.toList();
    }


    @Transactional
    public long deleteByIndex(MarketIndex index) {
        return repository.deleteByIndex(index);
    }


    public List<IndexUnit> findAllByIndexWithDateTimeEqualOrAfter(MarketIndex index, LocalDateTime dateTime) {
        String utcString = Du.toUtcString(dateTime);
        return repository.findAllByIndexWithDateTimeEqualOrAfter(index, utcString);
    }

    public int findFirstIdOf(MarketIndex index, LocalDateTime dateTime) {
        int id = -1;
        String utcString = Du.toUtcString(dateTime);
        List<IndexUnit> indexes = repository.findAllByIndexWithDateTimeEqualOrAfter(index, utcString, PageRequest.of(0, 1));
        if (indexes.size() == 1) {
            id = indexes.get(0).getId();
        }
        return id;
    }

    public int findFirstIdOf(MarketIndex index, LocalDate date) {
        LocalDateTime localDateTime = date.atStartOfDay();
        return findFirstIdOf(index, localDateTime);
    }

    public List<IndexUnit> findAllByIndexFromId(MarketIndex index, int id, int limit){
        PageRequest pageRequest = PageRequest.of(0, limit);
        List<IndexUnit> indexes = repository.findAllByIndexFromId(index, id, pageRequest);
        return indexes;
    }

    public int countBy(MarketIndex index) {
        IndexUnit entity = new IndexUnit();
        MarketIndex eIndex = new MarketIndex();
        eIndex.setId(index.getId());
        entity.setIndex(eIndex);
        int count = (int) repository.count(Example.of(entity));
        return count;
    }


    /**
     * Return the average price in the specified period
     */
    public float getAveragePrice(MarketIndex index, LocalDateTime t1, LocalDateTime t2){
        String st1 = Du.toUtcString(t1);
        String st2 = Du.toUtcString(t2);
        List<IndexUnit> units = repository.findAllByPeriod(index, st1, st2, Pageable.unpaged());
        if(units.size()==0){
            return 0;
        }
        AtomicReference<Float> totPrice= new AtomicReference<>((float) 0);
        units.stream().forEach(e -> totPrice.updateAndGet(v -> (float) (v + e.getClose())));

        float avg = totPrice.get() / units.size();
        return avg;
    }



    @Override
    protected IndexUnitRepository getRepository() {
        return repository;
    }

}
