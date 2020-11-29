package jmri.jmrit.display.controlPanelEditor.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * PortalIconXmlTest.java
 *
 * Test for the PortalIconXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class PortalIconXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("PortalIconXml constructor",new PortalIconXml());
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

