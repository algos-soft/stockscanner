package com.algos.stockscanner.data.service;

import com.algos.stockscanner.beans.Utils;
import com.algos.stockscanner.data.entity.MarketIndex;
import com.algos.stockscanner.data.enums.FrequencyTypes;
import com.algos.stockscanner.data.enums.IndexCategories;
import com.algos.stockscanner.utils.Du;
import com.algos.stockscanner.views.indexes.IndexModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.vaadin.artur.helpers.CrudService;

import java.util.List;
import java.util.Optional;

@Service
public class AdminService {

    @Autowired
    private Utils utils;

    public AdminService() {
    }

}
