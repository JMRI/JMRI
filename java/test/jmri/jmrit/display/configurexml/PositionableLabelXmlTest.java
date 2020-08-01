package jmri.jmrit.display.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * PositionableLabelXmlTest.java
 *
 * Test for the PositionableLabelXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class PositionableLabelXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("PositionableLabelXml constructor",new PositionableLabelXml());
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

