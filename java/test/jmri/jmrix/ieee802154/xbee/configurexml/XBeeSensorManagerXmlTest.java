package jmri.jmrix.ieee802154.xbee.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * XBeeSensorManagerXmlTest.java
 *
 * Test for the XBeeSensorManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class XBeeSensorManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("XBeeSensorManagerXml constructor",new XBeeSensorManagerXml());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}

