package jmri.jmrix.rfid.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * RfidSensorManagerXmlTest.java
 *
 * Description: tests for the RfidSensorManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class RfidSensorManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("RfidSensorManagerXml constructor",new RfidSensorManagerXml());
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

