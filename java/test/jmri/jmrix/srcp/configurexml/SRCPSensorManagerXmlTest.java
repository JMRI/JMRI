package jmri.jmrix.srcp.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * SRCPSensorManagerXmlTest.java
 *
 * Description: tests for the SRCPSensorManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class SRCPSensorManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("SRCPSensorManagerXml constructor",new SRCPSensorManagerXml());
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

