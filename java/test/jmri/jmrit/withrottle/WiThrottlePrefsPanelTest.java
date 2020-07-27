package jmri.jmrit.withrottle;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Test simple functioning of WiThrottlePrefsPanel
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class WiThrottlePrefsPanelTest {

    @Test
    public void testCtor() {
        WiThrottlePrefsPanel panel = new WiThrottlePrefsPanel();
        Assert.assertNotNull("exists", panel );
    }

    @BeforeEach
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initDebugThrottleManager();
        JUnitUtil.initStartupActionsManager();
    }
    
    @AfterEach
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }
}
