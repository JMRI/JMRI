package jmri.jmrix.rfid.merg.concentrator.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * ConcentratorSensorManagerXmlTest.java
 *
 * Description: tests for the ConcentratorSensorManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class ConcentratorSensorManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("ConcentratorSensorManagerXml constructor",new ConcentratorSensorManagerXml());
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

