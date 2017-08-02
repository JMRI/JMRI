package jmri.jmrix.lenz.configurexml;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * XNetLightManagerXmlTest.java
 *
 * Description: tests for the XNetLightManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class XNetLightManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("XNetLightManagerXml constructor",new XNetLightManagerXml());
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

