package jmri.jmrit.throttle;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of ControlPanelPropertyEditor
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class ControlPanelPropertyEditorTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ControlPanel panel = new ControlPanel();
        ControlPanelPropertyEditor editor = new ControlPanelPropertyEditor(panel);
        Assert.assertNotNull("exists", editor);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.resetWindows(false,false);
        JUnitUtil.tearDown();
    }
}
