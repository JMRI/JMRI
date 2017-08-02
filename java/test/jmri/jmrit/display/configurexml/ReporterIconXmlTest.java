package jmri.jmrit.display.configurexml;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * ReporterIconXmlTest.java
 *
 * Description: tests for the ReporterIconXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class ReporterIconXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("ReporterIconXml constructor",new ReporterIconXml());
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

