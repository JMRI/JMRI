package jmri.jmrix.ieee802154.xbee.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * XBeeSensorManagerXmlTest.java
 *
 * Description: tests for the XBeeSensorManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class XBeeSensorManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("XBeeSensorManagerXml constructor",new XBeeSensorManagerXml());
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

