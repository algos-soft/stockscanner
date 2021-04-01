package com.algos.stockscanner.data.service;

import com.algos.stockscanner.data.entity.SamplePerson;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;

public interface SamplePersonRepository extends JpaRepository<SamplePerson, Integer> {

}