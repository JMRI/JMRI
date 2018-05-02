package jmri.jmrix.dccpp.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * DCCppSensorManagerXmlTest.java
 *
 * Description: tests for the DCCppSensorManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class DCCppSensorManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("DCCppSensorManagerXml constructor",new DCCppSensorManagerXml());
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

