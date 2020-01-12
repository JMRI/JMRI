package jmri.jmrit.display.layoutEditor.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * LayoutShapeXmlTest.java
 *
 * Description: tests for the LayoutShapeXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class LayoutShapeXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("LayoutShapeXml constructor",new LayoutShapeXml());
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

