package jmri.jmrit.display.panelEditor.configurexml;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * PanelEditorXmlTest.java
 *
 * Description: tests for the PanelEditorXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class PanelEditorXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("PanelEditorXml constructor",new PanelEditorXml());
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

