package jmri.jmrit.display.layoutEditor.configurexml;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * LayoutEditorXmlTest.java
 *
 * Description: tests for the LayoutEditorXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class LayoutEditorXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("LayoutEditorXml constructor",new LayoutEditorXml());
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

