package jmri.jmrit.display.controlPanelEditor.shape.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * PositionableCircleXmlTest.java
 *
 * Description: tests for the PositionableCircleXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class PositionableCircleXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("PositionableCircleXml constructor",new PositionableCircleXml());
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

