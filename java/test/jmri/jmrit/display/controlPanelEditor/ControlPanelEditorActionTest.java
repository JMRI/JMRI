package jmri.jmrit.display.controlPanelEditor;

import java.awt.GraphicsEnvironment;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;
import org.junit.Assume;

/**
 * Test simple functioning of the ControlPanelEditorAction class.
 *
 * @author  Paul Bender Copyright (C) 2017 
 */
public class ControlPanelEditorActionTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ControlPanelEditorAction cpea = new ControlPanelEditorAction();
        Assert.assertNotNull("exists", cpea );
    }

    @Test
    public void testStringCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ControlPanelEditorAction cpea = new ControlPanelEditorAction("Test Action");
        Assert.assertNotNull("exists", cpea );
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
