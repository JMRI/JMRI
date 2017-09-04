package jmri.jmrix.dcc4pc.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Dcc4PcSensorManagerXmlTest.java
 *
 * Description: tests for the Dcc4PcSensorManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class Dcc4PcSensorManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("Dcc4PcSensorManagerXml constructor",new Dcc4PcSensorManagerXml());
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

