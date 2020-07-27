package jmri.jmrix.rfid.merg.concentrator.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * ConcentratorSensorManagerXmlTest.java
 *
 * Test for the ConcentratorSensorManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class ConcentratorSensorManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("ConcentratorSensorManagerXml constructor",new ConcentratorSensorManagerXml());
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

