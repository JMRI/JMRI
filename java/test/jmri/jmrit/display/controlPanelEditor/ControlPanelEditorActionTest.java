package jmri.jmrit.display.controlPanelEditor;

import apps.tests.Log4JFixture;
import jmri.InstanceManager;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import java.awt.GraphicsEnvironment;

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
        Log4JFixture.setUp();
        JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.resetInstanceManager();
        Log4JFixture.tearDown();
    }

}
