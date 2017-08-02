package jmri.jmrix.grapevine.configurexml;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * SerialSignalHeadXmlTest.java
 *
 * Description: tests for the SerialSignalHeadXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class SerialSignalHeadXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("SerialSignalHeadXml constructor",new SerialSignalHeadXml());
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

