package jmri.jmrit.display.layoutEditor.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * LayoutShapeXmlTest.java
 *
 * Test for the LayoutShapeXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class LayoutShapeXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("LayoutShapeXml constructor",new LayoutShapeXml());
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

