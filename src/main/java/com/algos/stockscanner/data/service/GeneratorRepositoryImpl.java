package com.algos.stockscanner.data.service;

import com.algos.stockscanner.data.entity.Generator;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public class GeneratorRepositoryImpl implements GeneratorRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    public void detach(Generator generator) {
        em.detach(generator);
    }
}
