package jmri.jmrit.withrottle;

import jmri.util.JUnitUtil;
import org.junit.Test;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

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
        apps.tests.Log4JFixture.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initDebugThrottleManager();
        JUnitUtil.initStartupActionsManager();
    }
    
    @After
    public void tearDown() throws Exception {
        JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }
}
