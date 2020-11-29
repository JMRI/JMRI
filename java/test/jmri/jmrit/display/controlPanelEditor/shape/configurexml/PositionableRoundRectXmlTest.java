package jmri.jmrit.display.controlPanelEditor.shape.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * PositionableRoundRectXmlTest.java
 *
 * Test for the PositionableRoundRectXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class PositionableRoundRectXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("PositionableRoundRectXml constructor",new PositionableRoundRectXml());
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

