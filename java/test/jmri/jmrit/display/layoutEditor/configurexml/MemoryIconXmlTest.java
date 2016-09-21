package jmri.jmrit.display.layoutEditor.configurexml;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * MemoryIconXmlTest.java
 *
 * Description: tests for the MemoryIconXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class MemoryIconXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("MemoryIconXml constructor",new MemoryIconXml());
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

