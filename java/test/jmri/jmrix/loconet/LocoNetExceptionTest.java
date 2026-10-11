package jmri.jmrix.loconet;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for LocoNetException class.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class LocoNetExceptionTest {

    @Test
    public void stringConstructorLnETest(){
        Assertions.assertNotNull( new LocoNetException("test exception"),
            "LocoNetException string constructor");
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
