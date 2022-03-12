package jmri.jmrit.throttle;

import java.awt.GraphicsEnvironment;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

/**
 * Test simple functioning of ThrottleFramePropertyEditor
 *
 * @author Paul Bender Copyright (C) 2016
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

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();
        JUnitUtil.initDebugThrottleManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.resetWindows(false,false);
        JUnitUtil.clearShutDownManager();
        JUnitUtil.tearDown();
    }
}
