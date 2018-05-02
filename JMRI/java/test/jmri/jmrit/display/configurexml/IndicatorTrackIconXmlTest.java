package jmri.jmrit.display.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * IndicatorTrackIconXmlTest.java
 *
 * Description: tests for the IndicatorTrackIconXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class IndicatorTrackIconXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("IndicatorTrackIconXml constructor",new IndicatorTrackIconXml());
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

