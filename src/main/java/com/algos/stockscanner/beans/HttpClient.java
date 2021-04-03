package com.algos.stockscanner.beans;


import okhttp3.OkHttpClient;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

@org.springframework.stereotype.Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class HttpClient extends OkHttpClient{
}
