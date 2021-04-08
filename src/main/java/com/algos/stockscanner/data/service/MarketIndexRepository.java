package com.algos.stockscanner.data.service;

import com.algos.stockscanner.data.entity.MarketIndex;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MarketIndexRepository extends JpaRepository<MarketIndex, Integer> {

    List<MarketIndex> findBySymbol (String symbol);

//    @Query("SELECT m FROM MarketIndex m WHERE m.symbol LIKE :value%")
//    List<MarketIndex> findBySymbolStartsWith (@Param("value") String value);

//    @Query("SELECT m FROM MarketIndex m WHERE m.symbol LIKE :value%")
//    List<MarketIndex> findBySymbolStartsWith (@Param("value") String value, Pageable pageable);


}