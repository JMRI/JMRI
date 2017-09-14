package jmri.jmrit.display.controlPanelEditor.shape.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * PositionableRectangleXmlTest.java
 *
 * Description: tests for the PositionableRectangleXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class PositionableRectangleXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("PositionableRectangleXml constructor",new PositionableRectangleXml());
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

