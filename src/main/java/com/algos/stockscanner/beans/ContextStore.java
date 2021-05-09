package com.algos.stockscanner.beans;

import com.algos.stockscanner.services.DownloadIndexCallable;
import com.algos.stockscanner.services.UpdateIndexDataCallable;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton used as a storage of objects common to all the application
 * (not related to sessions or UI but to the whole Spring instance on the server)
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class ContextStore {

    public ConcurrentHashMap<String, UpdateIndexDataCallable> updateIndexCallableMap = new ConcurrentHashMap<>();

    public ConcurrentHashMap<String, DownloadIndexCallable> downloadIndexCallableMap = new ConcurrentHashMap<>();




}
