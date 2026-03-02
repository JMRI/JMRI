package jmri.configurexml;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for JmriConfigureXmlException class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class JmriConfigureXmlExceptionTest {

    @Test
    public void testCtor(){
        Assertions.assertNotNull(new JmriConfigureXmlException(),
            "JmriConfigureXmlException constructor");
    }

    @Test
    public void testStringConstructorXmlException(){
        Assertions.assertNotNull(new JmriConfigureXmlException("test exception"),
                "JmriConfigureXmlException string constructor");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();

        JUnitUtil.initDefaultUserMessagePreferences();
    }

    @AfterEach
    public void tearDown(){
        JUnitUtil.tearDown();
    }

}
