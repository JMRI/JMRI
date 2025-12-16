package jmri.jmrix.mrc;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for MrcException class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/
public class MrcExceptionTest {

    @Test
    public void testMrcExceptionTestStringConstructor(){
        Assertions.assertNotNull( new MrcException("test exception"), "MrcException string constructor");
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
