package jmri.jmrit.display.switchboardEditor;

import java.awt.GraphicsEnvironment;
import jmri.jmrit.display.AbstractEditorTestBase;
import jmri.util.ColorUtil;
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
public class SwitchboardEditorTest extends AbstractEditorTestBase {

    private SwitchboardEditor swe = null;

    @Test
    public void testDefaultCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("exists", new SwitchboardEditor());
    }

    @Test
    public void testStringCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("exists", swe);
    }

    @Test
    public void testIsDirty() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // defaults to false.
        Assert.assertFalse("isDirty", swe.isDirty());
        JUnitUtil.dispose(swe);
    }

    @Test
    public void testSetDirty() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // defaults to false, setDirty() sets it to true.
        swe.setDirty();
        Assert.assertTrue("isDirty after set", swe.isDirty());
    }

    @Test
    public void testSetDirtyWithParameter() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // defaults to false, so set it to true.
        swe.setDirty(true);
        Assert.assertTrue("isDirty after set", swe.isDirty());
    }

    @Test
    public void testResetDirty() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // defaults to false, so set it to true.
        swe.setDirty(true);
        // then call resetDirty, which sets it back to false.
        swe.resetDirty();
        Assert.assertFalse("isDirty after reset", swe.isDirty());
    }

    @Test
    public void testGetDefaultTextColor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertEquals("Default Text Color", ColorUtil.ColorBlack, swe.getDefaultTextColor());
    }

    @Test
    public void testSetDefaultTextColor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        swe.setDefaultTextColor(ColorUtil.ColorPink);
        Assert.assertEquals("Default Text Color after Set", ColorUtil.ColorPink, swe.getDefaultTextColor());
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        if (!GraphicsEnvironment.isHeadless()) {
            jmri.util.JUnitUtil.resetProfileManager();
            e = swe = new SwitchboardEditor("Test Layout");
        }
    }

    @After
    public void tearDown() {
        if (swe != null) {
            JUnitUtil.dispose(swe);
            JUnitUtil.dispose(swe.getTargetFrame());
            e = swe = null;
        }
        JUnitUtil.tearDown();
    }

}
