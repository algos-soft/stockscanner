package com.algos.stockscanner.data.service;

import com.algos.stockscanner.beans.*;
import com.algos.stockscanner.data.entity.*;

import com.algos.stockscanner.views.persons.*;
import com.vaadin.flow.data.provider.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.vaadin.artur.helpers.CrudService;

import java.util.*;

@Service
public class UserService extends CrudService<User, Integer> {

    @Autowired
    private Utils utils;
    private UserRepository repository;

    public UserService(@Autowired UserRepository repository) {
        this.repository = repository;
    }

    @Override
    protected UserRepository getRepository() {
        return repository;
    }

    public List<UserModel> fetch(int offset, int limit, Example<User> example, List<QuerySortOrder> orders) {

        Sort sort = utils.buildSort(orders);

        Pageable pageable = new OffsetBasedPageRequest(offset, limit, sort);

        Page<User> page;
        if (example != null) {
            page = repository.findAll(example, pageable);
        } else {
            page = repository.findAll(pageable);
        }

        List<UserModel> list = new ArrayList<>();
        for (User entity : page.toList()) {
            UserModel model = new UserModel();
            entityToModel(entity, model);
            list.add(model);
        }

        return list;
    }



    /**
     * Copy data from Entity to View Model
     */
    public void entityToModel(User entity, UserModel model) {
        model.setId(utils.toPrimitive(entity.getId()));
    }

    public int count() {
        return (int) repository.count();
    }

}
