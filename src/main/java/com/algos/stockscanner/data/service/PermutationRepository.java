package com.algos.stockscanner.data.service;

import com.algos.stockscanner.data.entity.Permutation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermutationRepository extends JpaRepository<Permutation, Integer> {

    //List<Permutation> findBySymbol(String symbol);


}