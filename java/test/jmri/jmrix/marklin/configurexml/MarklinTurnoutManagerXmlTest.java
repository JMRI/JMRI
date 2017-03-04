package jmri.jmrix.marklin.configurexml;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * MarklinTurnoutManagerXmlTest.java
 *
 * Description: tests for the MarklinTurnoutManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class MarklinTurnoutManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("MarklinTurnoutManagerXml constructor",new MarklinTurnoutManagerXml());
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

