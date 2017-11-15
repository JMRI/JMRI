package jmri.jmrix.anyma.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * UsbLightManagerXmlTest.java
 *
 * Description: tests for the UsbLightManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class UsbLightManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("UsbLightManagerXml constructor",new UsbLightManagerXml());
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

