package jmri.jmrix.dccpp.configurexml;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * DCCppSensorManagerXmlTest.java
 *
 * Description: tests for the DCCppSensorManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class DCCppSensorManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("DCCppSensorManagerXml constructor",new DCCppSensorManagerXml());
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

