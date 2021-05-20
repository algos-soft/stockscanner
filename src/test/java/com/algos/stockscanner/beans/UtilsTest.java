package com.algos.stockscanner.beans;

import com.algos.stockscanner.exceptions.InvalidBigNumException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@RunWith(MockitoJUnitRunner.class)
public class UtilsTest {

    @InjectMocks
    private Utils unit;

    @Before
    public void init() throws IOException {
    }

    @Test
    // null input must throw InvalidBigNumException
    public void convertBigNumTest1() {
        assertThrows(InvalidBigNumException.class, () -> {
            unit.convertBigNum("");
        });
    }

    @Test
    // no numeric part, must throw InvalidBigNumException
    public void convertBigNumTest2() {
        assertThrows(InvalidBigNumException.class, () -> {
            unit.convertBigNum("ABCDE");
        });
    }


    @Test
    // missing unit part, must throw InvalidBigNumException
    public void convertBigNumTest3() {
        assertThrows(InvalidBigNumException.class, () -> {
            unit.convertBigNum("2.56");
        });
    }

    @Test
    // more than one letter, must throw InvalidBigNumException
    public void convertBigNumTest4() {
        assertThrows(InvalidBigNumException.class, () -> {
            unit.convertBigNum("2.56MB");
        });
    }

    @Test
    // unknown unit, must throw InvalidBigNumException
    public void convertBigNumTest5() {
        assertThrows(InvalidBigNumException.class, () -> {
            unit.convertBigNum("2.56Q");
        });
    }

    @Test
    // valid unit, no exception expected
    public void convertBigNumTest6() {
        assertDoesNotThrow(() -> {
            unit.convertBigNum("2.56M");
        });
    }

    @Test
    // verify result
    public void convertBigNumTest7() throws InvalidBigNumException {
        long number = unit.convertBigNum("2.56M");
        assert (number==2560000);
    }

    @Test
    // verify result with comma as separator
    public void convertBigNumTest8() throws InvalidBigNumException {
        long number = unit.convertBigNum("2,56M");
        assert (number==2560000);
    }

    @Test
    // verify result with spaces in the middle
    public void convertBigNumTest9() throws InvalidBigNumException {
        long number = unit.convertBigNum("2,56 M");
        assert (number==2560000);
    }

    @Test
    // verify result with spaces everywhere
    public void convertBigNumTest10() throws InvalidBigNumException {
        long number = unit.convertBigNum("  2,56     M ");
        assert (number==2560000);
    }

    @Test
    // verify result with leading zeroes
    public void convertBigNumTest11() throws InvalidBigNumException {
        long number = unit.convertBigNum("00002,56 M");
        assert (number==2560000);
    }











}
