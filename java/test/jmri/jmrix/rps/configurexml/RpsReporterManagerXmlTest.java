package jmri.jmrix.rps.configurexml;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * RpsReporterManagerXmlTest.java
 *
 * Description: tests for the RpsReporterManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class RpsReporterManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("RpsReporterManagerXml constructor",new RpsReporterManagerXml());
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

