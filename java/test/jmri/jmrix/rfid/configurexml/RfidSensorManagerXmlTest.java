package jmri.jmrix.rfid.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * RfidSensorManagerXmlTest.java
 *
 * Test for the RfidSensorManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class RfidSensorManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("RfidSensorManagerXml constructor",new RfidSensorManagerXml());
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

