package com.algos.stockscanner.data.service;

import com.algos.stockscanner.data.entity.Simulation;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.time.LocalDate;

public interface SimulationRepository extends JpaRepository<Simulation, Integer> {

}