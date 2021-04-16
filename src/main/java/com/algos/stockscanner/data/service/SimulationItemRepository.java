package com.algos.stockscanner.data.service;

import com.algos.stockscanner.data.entity.SimulationItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SimulationItemRepository extends JpaRepository<SimulationItem, Integer> {


}

