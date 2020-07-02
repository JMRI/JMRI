package jmri.jmrix.dccpp.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * DCCppSensorManagerXmlTest.java
 *
 * Test for the DCCppSensorManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class DCCppSensorManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("DCCppSensorManagerXml constructor",new DCCppSensorManagerXml());
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

