package jmri.jmrix.cmri.serial.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * SerialSensorManagerXmlTest.java
 *
 * Test for the SerialSensorManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class SerialSensorManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("SerialSensorManagerXml constructor",new SerialSensorManagerXml());
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

