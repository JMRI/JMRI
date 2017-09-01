package jmri.jmrit.display.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * SensorIconXmlTest.java
 *
 * Description: tests for the SensorIconXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class SensorIconXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("SensorIconXml constructor",new SensorIconXml());
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

