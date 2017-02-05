package jmri.jmrix.dcc.configurexml;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * DccTurnoutManagerXmlTest.java
 *
 * Description: tests for the DccTurnoutManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class DccTurnoutManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("DccTurnoutManagerXml constructor",new DccTurnoutManagerXml());
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

