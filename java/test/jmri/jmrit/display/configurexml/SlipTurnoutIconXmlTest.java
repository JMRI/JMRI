package jmri.jmrit.display.configurexml;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * SlipTurnoutIconXmlTest.java
 *
 * Description: tests for the SlipTurnoutIconXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class SlipTurnoutIconXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("SlipTurnoutIconXml constructor",new SlipTurnoutIconXml());
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

