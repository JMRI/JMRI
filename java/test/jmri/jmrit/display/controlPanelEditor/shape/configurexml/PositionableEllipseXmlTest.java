package jmri.jmrit.display.controlPanelEditor.shape.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}

