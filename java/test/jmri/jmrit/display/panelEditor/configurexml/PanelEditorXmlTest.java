package jmri.jmrit.display.panelEditor.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * PanelEditorXmlTest.java
 *
 * Test for the PanelEditorXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class PanelEditorXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("PanelEditorXml constructor",new PanelEditorXml());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}

