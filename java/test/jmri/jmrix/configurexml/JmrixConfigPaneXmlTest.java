package jmri.jmrix.configurexml;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * JmrixConfigPaneXmlTest.java
 *
 * Description: tests for the JmrixConfigPaneXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class JmrixConfigPaneXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("JmrixConfigPaneXml constructor",new JmrixConfigPaneXml());
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

