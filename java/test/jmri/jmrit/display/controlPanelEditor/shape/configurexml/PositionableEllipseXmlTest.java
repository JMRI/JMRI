package jmri.jmrit.display.controlPanelEditor.shape.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * PositionableEllipseXmlTest.java
 *
 * Test for the PositionableEllipseXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class PositionableEllipseXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("PositionableEllipseXml constructor",new PositionableEllipseXml());
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

