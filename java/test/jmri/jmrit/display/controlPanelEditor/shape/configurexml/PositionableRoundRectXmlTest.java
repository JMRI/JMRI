package jmri.jmrit.display.controlPanelEditor.shape.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * PositionableRoundRectXmlTest.java
 *
 * Description: tests for the PositionableRoundRectXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class PositionableRoundRectXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("PositionableRoundRectXml constructor",new PositionableRoundRectXml());
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

