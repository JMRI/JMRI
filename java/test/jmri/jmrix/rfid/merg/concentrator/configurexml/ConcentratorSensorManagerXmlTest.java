package jmri.jmrix.rfid.merg.concentrator.configurexml;

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
        apps.tests.Log4JFixture.setUp();
    }

    @After
    public void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}

