package jmri.jmrit.withrottle;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of WiThrottlePrefsPanel
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class WiThrottlePrefsPanelTest {

    @Test
    public void testCtor() {
        WiThrottlePrefsPanel panel = new WiThrottlePrefsPanel();
        Assert.assertNotNull("exists", panel );
    }

    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initDebugThrottleManager();
        JUnitUtil.initStartupActionsManager();
    }
    
    @After
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }
}
