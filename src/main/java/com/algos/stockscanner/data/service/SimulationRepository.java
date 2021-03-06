package com.algos.stockscanner.data.service;

import com.algos.stockscanner.data.entity.Generator;
import com.algos.stockscanner.data.entity.IndexUnit;
import com.algos.stockscanner.data.entity.MarketIndex;
import com.algos.stockscanner.data.entity.Simulation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDate;
import java.util.List;

public interface SimulationRepository extends JpaRepository<Simulation, Integer> {

    @Query("SELECT COUNT(s) FROM Simulation s WHERE s.generator=:generator")
    int countByGenerator(@Param("generator") Generator generator);



}