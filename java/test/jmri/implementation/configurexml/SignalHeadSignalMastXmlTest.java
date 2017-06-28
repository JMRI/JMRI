package jmri.implementation.configurexml;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * SignalHeadSignalMastXmlTest.java
 *
 * Description: tests for the SignalHeadSignalMastXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class SignalHeadSignalMastXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("SignalHeadSignalMastXml constructor",new SignalHeadSignalMastXml());
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

