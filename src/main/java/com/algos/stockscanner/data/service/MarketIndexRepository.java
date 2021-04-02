package com.algos.stockscanner.data.service;

import com.algos.stockscanner.data.entity.MarketIndex;
import com.algos.stockscanner.data.entity.SamplePerson;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarketIndexRepository extends JpaRepository<MarketIndex, Integer> {

}