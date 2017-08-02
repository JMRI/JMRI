package jmri.jmrix.pi.configurexml;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * RaspberryPiTurnoutManagerXmlTest.java
 *
 * Description: tests for the RaspberryPiTurnoutManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class RaspberryPiTurnoutManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("RaspberryPiTurnoutManagerXml constructor",new RaspberryPiTurnoutManagerXml());
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

