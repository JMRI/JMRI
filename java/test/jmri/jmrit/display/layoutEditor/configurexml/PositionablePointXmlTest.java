package jmri.jmrit.display.layoutEditor.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * PositionablePointXmlTest.java
 *
 * Test for the PositionablePointXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class PositionablePointXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("PositionablePointXml constructor",new PositionablePointXml());
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

