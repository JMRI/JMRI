package jmri.jmrit.display.switchboardEditor;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of SwitchboardEditor
 *
 * @author	Paul Bender Copyright (C) 2016
 * @author	Egbert Broerse Copyright (C) 2017
 */
public class SwitchboardEditorTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SwitchboardEditor e = new SwitchboardEditor();
        Assert.assertNotNull("exists", e);
    }

    @Test
    public void testStringCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SwitchboardEditor e = new SwitchboardEditor("Test Layout");
        Assert.assertNotNull("exists", e);
        e.dispose();
    }

    @Test
    public void testSetSize() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SwitchboardEditor e = new SwitchboardEditor();
        e.setSize(100, 100);
        java.awt.Dimension d = e.getSize();
        // the java.awt.Dimension stores the values as floating point
        // numbers, but setSize expects integer parameters.
        Assert.assertEquals("Width Set", 100.0, d.getWidth(), 0.0);
        Assert.assertEquals("Height Set", 100.0, d.getHeight(), 0.0);
        e.dispose();
    }

//    @Test
//    public void testGetSetZoom() {
//        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
//        SwitchboardEditor e = new SwitchboardEditor();
//        Assert.assertEquals("Zoom Get", 1.0, e.getZoom(), 0.0);
//        Assert.assertEquals("Zoom Set", 3.33, e.setZoom(3.33), 0.0);
//        Assert.assertEquals("Zoom Get", 3.33, e.getZoom(), 0.0);
//    }

    @Test
    public void testIsDirty() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SwitchboardEditor e = new SwitchboardEditor();
        // defaults to false.
        Assert.assertFalse("isDirty", e.isDirty());
        e.dispose();
    }

    @Test
    public void testSetDirty() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SwitchboardEditor e = new SwitchboardEditor();
        // defaults to false, setDirty() sets it to true.
        e.setDirty();
        Assert.assertTrue("isDirty after set", e.isDirty());
        e.dispose();
    }

    @Test
    public void testSetDirtyWithParameter() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SwitchboardEditor e = new SwitchboardEditor();
        // defaults to false, so set it to true.
        e.setDirty(true);
        Assert.assertTrue("isDirty after set", e.isDirty());
        e.dispose();
    }

    @Test
    public void testResetDirty() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SwitchboardEditor e = new SwitchboardEditor();
        // defaults to false, so set it to true.
        e.setDirty(true);
        // then call resetDirty, which sets it back to false.
        e.resetDirty();
        Assert.assertFalse("isDirty after reset", e.isDirty());
        e.dispose();
    }

    @Test
    public void testGetDefaultTextColor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SwitchboardEditor e = new SwitchboardEditor();
        Assert.assertEquals("Default Text Color", "black", e.getDefaultTextColor());
        e.dispose();
    }

    @Test
    public void testSetDefaultTextColor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SwitchboardEditor e = new SwitchboardEditor();
        e.setDefaultTextColor("pink");
        Assert.assertEquals("Default Text Color after Set", "pink", e.getDefaultTextColor());
        e.dispose();
    }

//    @Test
//    public void testGetShowHelpBar() {
//        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
//        SwitchboardEditor e = new SwitchboardEditor();
//        // default to true
//        Assert.assertTrue("getShowHelpBar", e.getShowHelpBar());
//    }
//
//    @Test
//    public void testSetShowHelpBar() {
//        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
//        SwitchboardEditor e = new SwitchboardEditor();
//        // default to true, so set to false.
//        e.setShowHelpBar(false);
//        Assert.assertFalse("getShowHelpBar after set", e.getShowHelpBar());
//    }

    // from here down is testing infrastructure
    @Before
    public void setUp() throws Exception {
        apps.tests.Log4JFixture.setUp();
        // dispose of the single PanelMenu instance
        jmri.jmrit.display.PanelMenu.dispose();
        // reset the instance manager.
        JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() throws Exception {
        // dispose of the single PanelMenu instance
        jmri.jmrit.display.PanelMenu.dispose();
        JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

}
