package jmri.jmrit.throttle;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of ThrottleFramePropertyEditor
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class ThrottleFramePropertyEditorTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ThrottleWindow frame = new ThrottleWindow();
        frame.setVisible(true);
        ThrottleFramePropertyEditor dialog = new ThrottleFramePropertyEditor(frame);
        Assert.assertNotNull("exists", dialog);
        JUnitUtil.dispose(dialog);
        JUnitUtil.dispose(frame);
     }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.resetWindows(false,false);
        JUnitUtil.tearDown();
    }
}
