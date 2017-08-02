package jmri.jmrix.easydcc.configurexml;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * EasyDccTurnoutManagerXmlTest.java
 *
 * Description: tests for the EasyDccTurnoutManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class EasyDccTurnoutManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("EasyDccTurnoutManagerXml constructor",new EasyDccTurnoutManagerXml());
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

