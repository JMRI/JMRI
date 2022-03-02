package jmri.jmrix.ecos;

import jmri.InstanceManager;
import jmri.ShutDownManager;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * EcosSensorManagerTest.java
 *
 * Test for the EcosSensorManager class
 *
 * @author Paul Bender Copyright (C) 2012,2016
 */
public class EcosSensorManagerTest extends jmri.managers.AbstractSensorMgrTestBase {

    @Override
    public String getSystemName(int i) {
        return "US" + i;
    }

    private EcosTrafficController tc;

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();
        JUnitUtil.initDefaultUserMessagePreferences();
        tc = new EcosInterfaceScaffold();
        EcosSystemConnectionMemo memo = new EcosSystemConnectionMemo(tc);
        l = new EcosSensorManager(memo);
        InstanceManager.getDefault(ShutDownManager.class).deregister(memo.getPreferenceManager().ecosPreferencesShutDownTask);
    }

    @AfterEach
    public void tearDown() {
        l.dispose();
        tc.terminateThreads();
        l = null;
        tc = null;
        JUnitUtil.tearDown();
    }

}
