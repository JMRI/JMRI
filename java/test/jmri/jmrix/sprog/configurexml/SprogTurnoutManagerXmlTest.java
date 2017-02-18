package jmri.jmrix.sprog.configurexml;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * SprogTurnoutManagerXmlTest.java
 *
 * Description: tests for the SprogTurnoutManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class SprogTurnoutManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("SprogTurnoutManagerXml constructor",new SprogTurnoutManagerXml());
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

