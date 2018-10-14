package jmri.jmrix.can.cbus.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * CbusLightManagerXmlTest.java
 *
 * Description: tests for the CbusLightManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class CbusLightManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("CbusLightManagerXml constructor",new CbusLightManagerXml());
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

