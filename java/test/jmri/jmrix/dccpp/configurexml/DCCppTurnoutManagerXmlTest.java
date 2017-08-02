package jmri.jmrix.dccpp.configurexml;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * DCCppTurnoutManagerXmlTest.java
 *
 * Description: tests for the DCCppTurnoutManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class DCCppTurnoutManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("DCCppTurnoutManagerXml constructor",new DCCppTurnoutManagerXml());
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

