package com.algos.stockscanner.data.service;

import com.algos.stockscanner.data.entity.MarketIndex;
import com.algos.stockscanner.data.entity.SamplePerson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface MarketIndexRepository extends JpaRepository<MarketIndex, Integer> {

    List<MarketIndex> findBySymbol (String symbol);

//    @Query("SELECT i FROM MarketIndex i INNER JOIN FETCH i.units WHERE i.symbol = ?1 ")
//    List<MarketIndex> findBySymbolEager (String symbol);



}