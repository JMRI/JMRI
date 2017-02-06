package jmri.implementation.configurexml;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * QuadOutputSignalHeadXmlTest.java
 *
 * Description: tests for the QuadOutputSignalHeadXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class QuadOutputSignalHeadXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("QuadOutputSignalHeadXml constructor",new QuadOutputSignalHeadXml());
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

