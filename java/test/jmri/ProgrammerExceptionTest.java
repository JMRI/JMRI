package jmri;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests for ProgrammerException class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/
public class ProgrammerExceptionTest {

    @Test
    public void testConstructor(){
        assertNotNull( new ProgrammerException(), "ProgrammerException constructor");
    }

    @Test
    public void testStringConstructor(){
        assertNotNull( new ProgrammerException("test exception"), "ProgrammerException string constructor");
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
