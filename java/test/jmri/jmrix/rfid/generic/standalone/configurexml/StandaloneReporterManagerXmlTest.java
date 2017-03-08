package jmri.jmrix.rfid.generic.standalone.configurexml;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * StandaloneReporterManagerXmlTest.java
 *
 * Description: tests for the StandaloneReporterManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class StandaloneReporterManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("StandaloneReporterManagerXml constructor",new StandaloneReporterManagerXml());
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

