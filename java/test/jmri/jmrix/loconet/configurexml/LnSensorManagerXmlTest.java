package jmri.jmrix.loconet.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * LnSensorManagerXmlTest.java
 *
 * Test for the LnSensorManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class LnSensorManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("LnSensorManagerXml constructor",new LnSensorManagerXml());
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

