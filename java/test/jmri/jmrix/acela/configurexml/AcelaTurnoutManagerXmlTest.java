package jmri.jmrix.acela.configurexml;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * AcelaTurnoutManagerXmlTest.java
 *
 * Description: tests for the AcelaTurnoutManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class AcelaTurnoutManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("AcelaTurnoutManagerXml constructor",new AcelaTurnoutManagerXml());
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

