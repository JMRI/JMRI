package jmri.jmrix.marklin.configurexml;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * MarklinSensorManagerXmlTest.java
 *
 * Description: tests for the MarklinSensorManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class MarklinSensorManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("MarklinSensorManagerXml constructor",new MarklinSensorManagerXml());
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

