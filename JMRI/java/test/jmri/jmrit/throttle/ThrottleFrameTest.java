package jmri.jmrit.throttle;

import java.awt.GraphicsEnvironment;
import jmri.InstanceManager;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of ThrottleFrame
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class ThrottleFrameTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ThrottleWindow frame = new ThrottleWindow();
        ThrottleFrame panel = new ThrottleFrame(frame);
        Assert.assertNotNull("exists", panel);
        JUnitUtil.dispose(frame);
        // the throttle list frame gets created above, but needs to be shown to be disposed
        InstanceManager.getDefault(ThrottleFrameManager.class).showThrottlesList();
        JUnitUtil.disposeFrame(Bundle.getMessage("ThrottleListFrameTile"), true, true);
    }

    @After
    public void setUp() {
        JUnitUtil.setUp();
    }

    @Before
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
