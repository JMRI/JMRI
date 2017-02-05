package jmri.jmrix.pi.configurexml;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * RaspberryPiSensorManagerXmlTest.java
 *
 * Description: tests for the RaspberryPiSensorManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class RaspberryPiSensorManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("RaspberryPiSensorManagerXml constructor",new RaspberryPiSensorManagerXml());
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

