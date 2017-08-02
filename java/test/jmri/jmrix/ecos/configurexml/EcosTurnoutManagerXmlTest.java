package jmri.jmrix.ecos.configurexml;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * EcosTurnoutManagerXmlTest.java
 *
 * Description: tests for the EcosTurnoutManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class EcosTurnoutManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("EcosTurnoutManagerXml constructor",new EcosTurnoutManagerXml());
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

