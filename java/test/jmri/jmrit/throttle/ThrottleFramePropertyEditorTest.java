package jmri.jmrit.throttle;

import jmri.InstanceManager;
import jmri.util.*;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of ThrottleFramePropertyEditor
 *
 * @author Paul Bender Copyright (C) 2016
 */
@DisabledIfHeadless
public class ThrottleFramePropertyEditorTest {

    @Test
    public void testCtor() {

        // this will disable svg icons, batik randomly crashes JUnit tests
        InstanceManager.getDefault(ThrottlesPreferences.class).setUseLargeSpeedSlider(false);
        ThrottleWindow frame = new ThrottleWindow();
        ThreadingUtil.runOnGUI( () -> frame.setVisible(true) );
        ThrottleFramePropertyEditor dialog = new ThrottleFramePropertyEditor(frame);
        Assertions.assertNotNull( dialog, "exists");
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
        JmriJFrame throttleListFrame = JmriJFrame.getFrame(Bundle.getMessage("ThrottleListFrameTile"));
        if ( throttleListFrame != null ) {
            JUnitUtil.dispose(throttleListFrame);
        }
        JUnitUtil.clearShutDownManager();
        JUnitUtil.tearDown();
    }
}
