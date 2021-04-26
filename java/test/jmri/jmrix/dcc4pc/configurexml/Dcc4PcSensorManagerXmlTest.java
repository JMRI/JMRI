package jmri.jmrix.dcc4pc.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Dcc4PcSensorManagerXmlTest.java
 *
 * Test for the Dcc4PcSensorManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class Dcc4PcSensorManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("Dcc4PcSensorManagerXml constructor",new Dcc4PcSensorManagerXml());
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

