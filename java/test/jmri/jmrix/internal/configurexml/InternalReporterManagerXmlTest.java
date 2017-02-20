package jmri.jmrix.internal.configurexml;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * InternalReporterManagerXmlTest.java
 *
 * Description: tests for the InternalReporterManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class InternalReporterManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("InternalReporterManagerXml constructor",new InternalReporterManagerXml());
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

