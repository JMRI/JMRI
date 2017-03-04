package jmri.jmrix.nce.configurexml;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * NceTurnoutManagerXmlTest.java
 *
 * Description: tests for the NceTurnoutManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class NceTurnoutManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("NceTurnoutManagerXml constructor",new NceTurnoutManagerXml());
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

