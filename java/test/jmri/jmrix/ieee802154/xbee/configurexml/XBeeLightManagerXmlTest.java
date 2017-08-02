package jmri.jmrix.ieee802154.xbee.configurexml;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * XBeeLightManagerXmlTest.java
 *
 * Description: tests for the XBeeLightManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class XBeeLightManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("XBeeLightManagerXml constructor",new XBeeLightManagerXml());
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

