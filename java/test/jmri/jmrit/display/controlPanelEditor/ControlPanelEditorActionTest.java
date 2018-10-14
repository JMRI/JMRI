package jmri.jmrit.display.controlPanelEditor;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

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

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
