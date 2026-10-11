package apps.configurexml;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * SystemConsoleConfigPanelXmlTest.java
 *
 * Test for the SystemConsoleConfigPanelXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class SystemConsoleConfigPanelXmlTest {

    @Test
    public void testCtor(){
        Assertions.assertNotNull(new SystemConsoleConfigPanelXml(), "SystemConsoleConfigPanelXml constructor");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}

