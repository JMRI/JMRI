package jmri.jmrit.consisttool;

import jmri.InstanceManager;
import jmri.ConsistManager;
import jmri.swing.PreferencesPanelTestBase;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test simple functioning of ConsistToolPrefsPanel 
 *
 * @author Paul Bender Copyright (C) 2019
 */
public class ConsistToolPrefsPanelTest extends PreferencesPanelTestBase<ConsistToolPrefsPanel> {

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initDebugThrottleManager();
        JUnitUtil.initStartupActionsManager();
        InstanceManager.setDefault(ConsistPreferencesManager.class,new ConsistPreferencesManager());
        InstanceManager.setDefault(ConsistManager.class, new TestConsistManager());
        prefsPanel = new ConsistToolPrefsPanel();
    }

}
