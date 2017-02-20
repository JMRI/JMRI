package jmri.jmrix.cmri.serial.configurexml;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * SerialTurnoutManagerXmlTest.java
 *
 * Description: tests for the SerialTurnoutManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class SerialTurnoutManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("SerialTurnoutManagerXml constructor",new SerialTurnoutManagerXml());
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

