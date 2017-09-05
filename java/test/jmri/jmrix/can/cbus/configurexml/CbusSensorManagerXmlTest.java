package jmri.jmrix.can.cbus.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * CbusSensorManagerXmlTest.java
 *
 * Description: tests for the CbusSensorManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class CbusSensorManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("CbusSensorManagerXml constructor",new CbusSensorManagerXml());
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

