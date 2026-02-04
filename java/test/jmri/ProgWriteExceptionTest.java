package jmri;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests for ProgWriteException class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/
public class ProgWriteExceptionTest {

    @Test
    public void testCconstructor(){
        assertNotNull( new ProgWriteException(), "ProgWriteException constructor");
    }

    @Test
    public void testStringConstructor(){
        assertNotNull( new ProgWriteException("test exception"), "ProgWriteException string constructor");
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
