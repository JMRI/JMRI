package jmri;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests for TimebaseRateException class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/
public class TimebaseRateExceptionTest {

    @Test
    public void testConstructor(){
        assertNotNull( new TimebaseRateException(), "TimebaseRateException constructor");
    }

    @Test
    public void testStringConstructor(){
        assertNotNull( new TimebaseRateException("test exception"), "TimebaseRateException string constructor");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();

        JUnitUtil.initDefaultUserMessagePreferences();
    }

    @AfterEach
    public void tearDown(){
        JUnitUtil.tearDown();
    }

}
