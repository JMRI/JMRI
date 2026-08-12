package jmri.jmrit.progsupport;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for ProgModeException class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/
public class ProgModeExceptionTest {

    @Test
    public void testProgModeExceptionConstructor(){
        assertNotNull(new ProgModeException(), "ProgModeException constructor");
    }

    @Test
    public void testProgModeExceptionStringConstructor(){
        assertNotNull(new ProgModeException("test exception"), "ProgModeException string constructor");
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
