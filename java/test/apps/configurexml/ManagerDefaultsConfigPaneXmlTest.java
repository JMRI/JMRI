package apps.configurexml;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

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
        Assertions.assertNotNull(new ManagerDefaultsConfigPaneXml(), "ManagerDefaultsConfigPaneXml constructor");
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

