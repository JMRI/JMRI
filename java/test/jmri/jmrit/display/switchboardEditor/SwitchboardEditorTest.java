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
 * @author Paul Bender Copyright (C) 2016
 * @author Egbert Broerse Copyright (C) 2017
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
        JUnitUtil.dispose(e);
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
        JUnitUtil.dispose(e);
    }

    @Test
    public void testIsDirty() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SwitchboardEditor e = new SwitchboardEditor();
        // defaults to false.
        Assert.assertFalse("isDirty", e.isDirty());
        JUnitUtil.dispose(e);
    }

    @Test
    public void testSetDirty() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SwitchboardEditor e = new SwitchboardEditor();
        // defaults to false, setDirty() sets it to true.
        e.setDirty();
        Assert.assertTrue("isDirty after set", e.isDirty());
        JUnitUtil.dispose(e);
    }

    @Test
    public void testSetDirtyWithParameter() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SwitchboardEditor e = new SwitchboardEditor();
        // defaults to false, so set it to true.
        e.setDirty(true);
        Assert.assertTrue("isDirty after set", e.isDirty());
        JUnitUtil.dispose(e);
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
        JUnitUtil.dispose(e);
    }

    @Test
    public void testGetDefaultTextColor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SwitchboardEditor e = new SwitchboardEditor();
        Assert.assertEquals("Default Text Color",jmri.util.ColorUtil.ColorBlack, e.getDefaultTextColor());
        JUnitUtil.dispose(e);
    }

    @Test
    public void testSetDefaultTextColor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SwitchboardEditor e = new SwitchboardEditor();
        e.setDefaultTextColor(jmri.util.ColorUtil.ColorPink);
        Assert.assertEquals("Default Text Color after Set",jmri.util.ColorUtil.ColorPink, e.getDefaultTextColor());
        JUnitUtil.dispose(e);
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
