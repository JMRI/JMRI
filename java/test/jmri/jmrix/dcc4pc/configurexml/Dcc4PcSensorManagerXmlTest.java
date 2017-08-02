package jmri.jmrix.dcc4pc.configurexml;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Dcc4PcSensorManagerXmlTest.java
 *
 * Description: tests for the Dcc4PcSensorManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class Dcc4PcSensorManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("Dcc4PcSensorManagerXml constructor",new Dcc4PcSensorManagerXml());
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

