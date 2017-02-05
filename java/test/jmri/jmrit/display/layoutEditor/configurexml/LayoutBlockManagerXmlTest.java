package jmri.jmrit.display.layoutEditor.configurexml;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * LayoutBlockManagerXmlTest.java
 *
 * Description: tests for the LayoutBlockManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class LayoutBlockManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("LayoutBlockManagerXml constructor",new LayoutBlockManagerXml());
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

