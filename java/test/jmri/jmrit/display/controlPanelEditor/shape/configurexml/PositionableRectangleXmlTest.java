package jmri.jmrit.display.controlPanelEditor.shape.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * PositionableRectangleXmlTest.java
 *
 * Test for the PositionableRectangleXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class PositionableRectangleXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("PositionableRectangleXml constructor",new PositionableRectangleXml());
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

