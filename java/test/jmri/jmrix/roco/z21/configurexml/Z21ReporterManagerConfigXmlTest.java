package jmri.jmrix.roco.z21.configurexml;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Z21ReporterManagerConfigXml.java
 *
 * Description: tests for the Z21ReporterManagerConfigXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class Z21ReporterManagerConfigXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("Z21ReporterManagerConfigXml constructor",new Z21ReporterManagerConfigXml());
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

