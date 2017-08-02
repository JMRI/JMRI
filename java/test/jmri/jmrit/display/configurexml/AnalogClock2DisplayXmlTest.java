package jmri.jmrit.display.configurexml;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * AnalogClock2DisplayXmlTest.java
 *
 * Description: tests for the AnalogClock2DisplayXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class AnalogClock2DisplayXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("AnalogClock2DisplayXml constructor",new AnalogClock2DisplayXml());
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

