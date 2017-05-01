package jmri.jmrit.display.controlPanelEditor.configurexml;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * ControlPanelEditorXmlTest.java
 *
 * Description: tests for the ControlPanelEditorXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class ControlPanelEditorXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("ControlPanelEditorXml constructor",new ControlPanelEditorXml());
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

