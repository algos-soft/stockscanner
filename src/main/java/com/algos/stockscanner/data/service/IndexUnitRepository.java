package com.algos.stockscanner.data.service;

import com.algos.stockscanner.data.entity.IndexUnit;
import com.algos.stockscanner.data.entity.MarketIndex;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IndexUnitRepository extends JpaRepository<IndexUnit, Integer> {

    List<IndexUnit> findByIndex (MarketIndex index);

}