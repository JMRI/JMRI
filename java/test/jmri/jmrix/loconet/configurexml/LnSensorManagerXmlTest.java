package jmri.jmrix.loconet.configurexml;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * LnSensorManagerXmlTest.java
 *
 * Description: tests for the LnSensorManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class LnSensorManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("LnSensorManagerXml constructor",new LnSensorManagerXml());
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

