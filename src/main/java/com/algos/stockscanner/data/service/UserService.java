package com.algos.stockscanner.data.service;

import com.algos.stockscanner.beans.*;
import com.algos.stockscanner.data.entity.*;
import com.algos.stockscanner.views.users.*;
import com.vaadin.flow.data.provider.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.*;
import org.vaadin.artur.helpers.*;

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
        }
        else {
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
        model.setFirstName(entity.getFirstName());
        model.setLastName(entity.getLastName());
        model.setUserName(entity.getUserName());
        model.setPassword(entity.getPassword());
        model.setEmail(entity.getEmail());
        model.setPhone(entity.getPhone());
        model.setDateOfBirth(entity.getDateOfBirth());
        model.setImportant(entity.getImportant());
    }

    /**
     * Update entity from model
     */
    public void modelToEntity(UserModel model, User entity) {
        entity.setFirstName(model.getFirstName());
        entity.setLastName(model.getLastName());
        entity.setUserName(model.getUserName());
        entity.setPassword(model.getPassword());
        entity.setEmail(model.getEmail());
        entity.setPhone(model.getPhone());
        entity.setDateOfBirth(model.getDateOfBirth());
        entity.setImportant(model.getImportant());
    }

    /**
     * Standard initialization of a new entity for the database.
     * <p>
     * Initialize with default values
     */
    public void initEntity(User entity) {
        entity.setUserName("Nick");
        entity.setPassword("+++");
        entity.setEmail("...@libero.it");
        entity.setFirstName("Mario");
        entity.setLastName("Rossi");
    }

    /**
     * Standard initialization of a new model for the dialog.
     * <p>
     * Initialize with default values
     */
    public void initModel(UserModel model) {
        model.setUserName("Nick");
        model.setPassword("+++");
        model.setEmail("...@libero.it");
        model.setFirstName("Mario");
        model.setLastName("Rossi");
    }

    public int count() {
        return (int) repository.count();
    }

}
