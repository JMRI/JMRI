package jmri.jmrix.mrc;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for MrcMessageException class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/
public class MrcMessageExceptionTest {

    @Test
    public void testMrcMessageExceptionConstructor(){
        assertNotNull( new MrcMessageException(), "MrcMessageException constructor");
    }

    @Test
    public void testMrcMessageExceptionStringConstructor(){
        assertNotNull( new MrcMessageException("test exception"), "MrcMessageException string constructor");
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
