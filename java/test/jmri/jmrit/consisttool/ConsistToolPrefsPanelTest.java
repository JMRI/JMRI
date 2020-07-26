package jmri.jmrit.consisttool;

import jmri.InstanceManager;
import jmri.ConsistManager;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Test simple functioning of ConsistToolPrefsPanel 
 *
 * @author Paul Bender Copyright (C) 2019
 */
public class ConsistToolPrefsPanelTest {

    @Test
    public void testCtor() {
        ConsistToolPrefsPanel panel = new ConsistToolPrefsPanel();
        Assert.assertNotNull("exists", panel );
    }

    @BeforeEach
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initDebugThrottleManager();
        JUnitUtil.initStartupActionsManager();
        InstanceManager.setDefault(ConsistPreferencesManager.class,new ConsistPreferencesManager());
        InstanceManager.setDefault(ConsistManager.class, new TestConsistManager());
    }
    
    @AfterEach
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }
}
