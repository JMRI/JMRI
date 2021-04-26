package jmri.jmrit.display.controlPanelEditor.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * ControlPanelEditorXmlTest.java
 *
 * Test for the ControlPanelEditorXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class ControlPanelEditorXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("ControlPanelEditorXml constructor",new ControlPanelEditorXml());
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

