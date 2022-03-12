package jmri.jmrit.display.controlPanelEditor.shape.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * PositionablePolygonXmlTest.java
 *
 * Test for the PositionablePolygonXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class PositionablePolygonXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("PositionablePolygonXml constructor",new PositionablePolygonXml());
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

