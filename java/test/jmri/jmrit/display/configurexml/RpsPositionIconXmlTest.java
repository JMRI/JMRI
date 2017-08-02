package jmri.jmrit.display.configurexml;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * RpsPositionIconXmlTest.java
 *
 * Description: tests for the RpsPositionIconXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class RpsPositionIconXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("RpsPositionIconXml constructor",new RpsPositionIconXml());
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

