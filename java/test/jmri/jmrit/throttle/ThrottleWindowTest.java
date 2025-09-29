package jmri.jmrit.throttle;

import jmri.InstanceManager;
import jmri.util.JmriJFrame;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of ThrottleWindow
 *
 * @author Paul Bender Copyright (C) 2016
 */
@jmri.util.junit.annotations.DisabledIfHeadless
public class ThrottleWindowTest extends jmri.util.JmriJFrameTestBase {

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();
        JUnitUtil.initDebugThrottleManager();
        // this will disable svg icons, batik randomly crashes JUnit tests
        InstanceManager.getDefault(ThrottlesPreferences.class).setUseLargeSpeedSlider(false);
        frame = new ThrottleWindow();
    }

    @AfterEach
    @Override
    public void tearDown() {

        JmriJFrame throttleListFrame = JmriJFrame.getFrame(Bundle.getMessage("ThrottleListFrameTile"));
        if ( throttleListFrame != null ) {
            JUnitUtil.dispose(throttleListFrame);
        }

        JUnitUtil.clearShutDownManager();
        super.tearDown();
    }
}
