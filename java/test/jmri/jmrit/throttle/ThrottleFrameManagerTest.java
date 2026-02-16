package jmri.jmrit.throttle;

import jmri.InstanceManager;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of ThrottleFrameManager
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class ThrottleFrameManagerTest {

    @Test
    @DisabledIfHeadless
    public void testCtor() {

        // the constructor is private, but invoked by instance.
        ThrottleFrameManager frame = InstanceManager.getDefault(ThrottleFrameManager.class);
        Assertions.assertNotNull(frame, "exists");
        frame.showThrottlesList();
        JUnitUtil.disposeFrame(Bundle.getMessage("ThrottleListFrameTile"),true,true);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
