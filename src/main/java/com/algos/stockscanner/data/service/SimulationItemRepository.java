package com.algos.stockscanner.data.service;

import com.algos.stockscanner.data.entity.IndexUnit;
import com.algos.stockscanner.data.entity.MarketIndex;
import com.algos.stockscanner.data.entity.Simulation;
import com.algos.stockscanner.data.entity.SimulationItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SimulationItemRepository extends JpaRepository<SimulationItem, Integer> {

    @Query("SELECT u FROM SimulationItem u where u.simulation = :simulation ORDER BY u.timestamp ASC")
    List<SimulationItem> findBySimulationOrderByTimestamp(@Param("simulation") Simulation simulation);

}

