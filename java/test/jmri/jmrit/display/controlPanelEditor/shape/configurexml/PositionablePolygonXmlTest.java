package jmri.jmrit.display.controlPanelEditor.shape.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * PositionablePolygonXmlTest.java
 *
 * Description: tests for the PositionablePolygonXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class PositionablePolygonXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("PositionablePolygonXml constructor",new PositionablePolygonXml());
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}

