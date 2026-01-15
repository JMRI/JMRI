package jmri;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests for ProgReadException class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/
public class ProgReadExceptionTest {

    @Test
    public void testConstructor(){
        assertNotNull( new ProgReadException(), "ProgReadException constructor");
    }

    @Test
    public void testStringConstructor(){
        assertNotNull( new ProgReadException("test exception"), "ProgReadException string constructor");
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
