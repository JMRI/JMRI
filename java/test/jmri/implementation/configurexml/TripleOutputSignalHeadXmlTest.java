package jmri.implementation.configurexml;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * TripleOutputSignalHeadXmlTest.java
 *
 * Description: tests for the TripleOutputSignalHeadXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class TripleOutputSignalHeadXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("TripleOutputSignalHeadXml constructor",new TripleOutputSignalHeadXml());
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

