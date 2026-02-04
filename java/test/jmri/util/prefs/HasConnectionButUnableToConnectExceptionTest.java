package jmri.util.prefs;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for InitializationException class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/
public class HasConnectionButUnableToConnectExceptionTest {

    @Test
    public void testCtor(){
        Assertions.assertNotNull( new HasConnectionButUnableToConnectException("test exception",null),
            "HasConnectionButUnableToConnectException constructor");
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
