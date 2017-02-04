package jmri.jmrix.roco.z21.configurexml;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Z21ReporterManagerXml.java
 *
 * Description: tests for the Z21ReporterManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class Z21ReporterManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("Z21ReporterManagerXml constructor",new Z21ReporterManagerXml());
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

