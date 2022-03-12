package apps.configurexml;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * ManagerDefaultsConfigPaneXmlTest.java
 *
 * Test for the ManagerDefaultsConfigPaneXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class ManagerDefaultsConfigPaneXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("ManagerDefaultsConfigPaneXml constructor",new ManagerDefaultsConfigPaneXml());
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

