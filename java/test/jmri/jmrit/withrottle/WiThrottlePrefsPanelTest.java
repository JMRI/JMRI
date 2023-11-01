package jmri.jmrit.withrottle;

import jmri.swing.PreferencesPanelTestBase;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of WiThrottlePrefsPanel
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class WiThrottlePrefsPanelTest extends PreferencesPanelTestBase<WiThrottlePrefsPanel> {

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initDebugThrottleManager();
        JUnitUtil.initStartupActionsManager();
        prefsPanel = new WiThrottlePrefsPanel();
    }

}
