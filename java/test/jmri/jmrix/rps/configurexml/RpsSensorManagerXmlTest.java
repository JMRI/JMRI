package jmri.jmrix.rps.configurexml;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * RpsSensorManagerXmlTest.java
 *
 * Description: tests for the RpsSensorManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class RpsSensorManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("RpsSensorManagerXml constructor",new RpsSensorManagerXml());
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

