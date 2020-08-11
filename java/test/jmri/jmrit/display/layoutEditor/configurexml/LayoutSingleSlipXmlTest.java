package jmri.jmrit.display.layoutEditor.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * LayoutSlipXmlTest.java
 *
 * Test for the LayoutSlipXml class
 *
 * @author   George Warner  Copyright (C) 2017
 */
public class LayoutSingleSlipXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("LayoutSlipXml constructor", new LayoutSingleSlipXml());
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
