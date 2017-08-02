package jmri.jmrix.powerline.configurexml;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * SerialSensorManagerXmlTest.java
 *
 * Description: tests for the SerialSensorManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class SerialSensorManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("SerialSensorManagerXml constructor",new SerialSensorManagerXml());
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

