package com.algos.stockscanner.beans;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;

@RunWith(MockitoJUnitRunner.class)
public class UtilsTest {

    @InjectMocks
    private Utils utils;

    @Before
    public void init() throws IOException {
    }

    @Test
    // invalid content type must throw InvalidMimeTypeException
    public void myTest() {
    }

}
