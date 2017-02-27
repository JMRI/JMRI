package jmri.implementation.configurexml;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * TripleTurnoutSignalHeadXmlTest.java
 *
 * Description: tests for the TripleTurnoutSignalHeadXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class TripleTurnoutSignalHeadXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("TripleTurnoutSignalHeadXml constructor",new TripleTurnoutSignalHeadXml());
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

