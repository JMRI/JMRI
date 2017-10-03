package jmri.jmrit.display.layoutEditor.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * LayoutSlipXmlTest.java
 *
 * Description: tests for the LayoutSlipXml class
 *
 * @author   George Warner  Copyright (C) 2017
 */
public class LayoutSlipXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("LayoutSlipXml constructor",new LayoutSlipXml());
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
