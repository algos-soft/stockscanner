package com.algos.stockscanner.data.service;

import com.algos.stockscanner.data.entity.MarketIndex;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MarketIndexRepository extends JpaRepository<MarketIndex, Integer> {

    List<MarketIndex> findBySymbol (String symbol);

    @Query("select m from MarketIndex m")
    List<MarketIndex> findAllAndSort(Sort sort);

//    @Query("SELECT m FROM MarketIndex m WHERE m.symbol LIKE :value%")
//    List<MarketIndex> findBySymbolStartsWith (@Param("value") String value);

    @Query("SELECT m FROM MarketIndex m ORDER BY m.symbol")
    Page<MarketIndex> findAllOrderBySymbol (Pageable pageable);

    @Query("SELECT m FROM MarketIndex m WHERE m.symbol LIKE :filterSymbol% OR m.name LIKE %:filterName% ORDER BY m.symbol")
    Page<MarketIndex> findAllWithFilterOrderBySymbol (Pageable pageable, @Param("filterSymbol") String filterSymbol, @Param("filterName") String filterName);

}