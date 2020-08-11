package apps.configurexml;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

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
      Assert.assertNotNull("SystemConsoleConfigPanelXml constructor",new SystemConsoleConfigPanelXml());
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

