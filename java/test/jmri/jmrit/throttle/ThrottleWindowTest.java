package jmri.jmrit.throttle;

import java.awt.GraphicsEnvironment;

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
            frame = new ThrottleWindow();
        }
    }

    @AfterEach
    @Override
    public void tearDown() {
        super.tearDown();
    }
}
