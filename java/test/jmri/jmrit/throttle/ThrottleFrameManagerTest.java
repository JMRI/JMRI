package jmri.jmrit.throttle;

import java.awt.GraphicsEnvironment;
import jmri.InstanceManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of ThrottleFrameManager
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class ThrottleFrameManagerTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // the constructor is private, but invoked by instance.
        ThrottleFrameManager frame = InstanceManager.getDefault(ThrottleFrameManager.class);
        Assert.assertNotNull("exists", frame);
        frame.showThrottlesList();
        jmri.util.SwingTestCase.disposeFrame(Bundle.getMessage("ThrottleListFrameTile"),true,true);
    }

    @Before
    public void setUp() throws Exception {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() throws Exception {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }
}
