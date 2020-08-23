package jmri.jmrix.srcp.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * SRCPSensorManagerXmlTest.java
 *
 * Test for the SRCPSensorManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class SRCPSensorManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("SRCPSensorManagerXml constructor",new SRCPSensorManagerXml());
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

