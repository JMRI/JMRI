package apps.configurexml;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * SystemConsoleConfigPanelXmlTest.java
 *
 * Description: tests for the SystemConsoleConfigPanelXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class SystemConsoleConfigPanelXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("SystemConsoleConfigPanelXml constructor",new SystemConsoleConfigPanelXml());
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

