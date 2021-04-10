package com.algos.stockscanner.data.service;

import com.algos.stockscanner.data.entity.Generator;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GeneratorRepository extends JpaRepository<Generator, Integer> {

    public Generator findFirstByOrderByNumberDesc();

}