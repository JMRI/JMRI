package jmri.jmrix.rfid.generic.standalone.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * StandaloneSensorManagerXmlTest.java
 *
 * Test for the StandaloneSensorManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class StandaloneSensorManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("StandaloneSensorManagerXml constructor",new StandaloneSensorManagerXml());
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

