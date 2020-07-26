package jmri.jmrix.ecos;

import jmri.InstanceManager;
import jmri.ShutDownManager;
import jmri.jmrix.SystemConnectionMemoTestBase;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for the Bundle class
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class EcosSystemConnectionMemoTest extends SystemConnectionMemoTestBase<EcosSystemConnectionMemo> {

    @Override
    @Test
    public void testProvidesConsistManager() {
        Assert.assertTrue("Provides ConsistManager", scm.provides(jmri.ConsistManager.class));
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();
        JUnitUtil.initDefaultUserMessagePreferences();
        scm = new EcosSystemConnectionMemo();
        scm.setEcosTrafficController(new EcosInterfaceScaffold());
        scm.configureManagers();
        scm.getPreferenceManager().setPreferencesLoaded();
        InstanceManager.store(scm, EcosSystemConnectionMemo.class);
    }

    @AfterEach
    @Override
    public void tearDown() {
        InstanceManager.getDefault(ShutDownManager.class).deregister(scm.getLocoAddressManager().ecosLocoShutDownTask);
        scm.getLocoAddressManager().terminateThreads();
        scm.getTrafficController().terminateThreads();
        scm.dispose();
        JUnitUtil.tearDown();
    }

}
