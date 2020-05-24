package jmri.jmrit.display.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * SensorIconXmlTest.java
 *
 * Test for the SensorIconXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class SensorIconXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("SensorIconXml constructor",new SensorIconXml());
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}

