package jmri.jmrix.lenz.configurexml;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * XNetSensorManagerXmlTest.java
 *
 * Description: tests for the XNetSensorManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class XNetSensorManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("XNetSensorManagerXml constructor",new XNetSensorManagerXml());
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

