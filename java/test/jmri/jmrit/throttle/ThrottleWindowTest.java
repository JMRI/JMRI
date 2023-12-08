package jmri.jmrit.throttle;

import java.awt.GraphicsEnvironment;

import jmri.InstanceManager;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of ThrottleWindow
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class ThrottleWindowTest extends jmri.util.JmriJFrameTestBase {

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();
        JUnitUtil.initDebugThrottleManager();
        if (!GraphicsEnvironment.isHeadless()) {
            // this will disable svg icons, batik randomly crashes JUnit tests
            InstanceManager.getDefault(ThrottlesPreferences.class).setUseLargeSpeedSlider(false);
            frame = new ThrottleWindow();
        }
    }

    @AfterEach
    @Override
    public void tearDown() {
        JUnitUtil.clearShutDownManager();
        super.tearDown();
    }
}
