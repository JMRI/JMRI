package jmri.jmrit.display.configurexml;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * IndicatorTrackIconXmlTest.java
 *
 * Test for the IndicatorTrackIconXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class IndicatorTrackIconXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("IndicatorTrackIconXml constructor",new IndicatorTrackIconXml());
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

