package jmri.jmrix.ecos;

import jmri.*;
import jmri.jmrix.SystemConnectionMemoTestBase;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for EcosSystemConnectionMemo.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class EcosSystemConnectionMemoTest extends SystemConnectionMemoTestBase<EcosSystemConnectionMemo> {

    @Override
    @Test
    public void testProvidesConsistManager() {
        Assertions.assertTrue( scm.provides(ConsistManager.class), "Provides ConsistManager");
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();
        JUnitUtil.initDefaultUserMessagePreferences();
        scm = new EcosSystemConnectionMemo(new EcosInterfaceScaffold());
        scm.configureManagers();
        scm.getPreferenceManager().setPreferencesLoaded();
        InstanceManager.getDefault(ShutDownManager.class).deregister(scm.getPreferenceManager().ecosPreferencesShutDownTask);
        
        scm.getLocoAddressManager().terminateThreads();
        JUnitUtil.waitFor(() -> { return !scm.getLocoAddressManager().threadsRunning(); });
        InstanceManager.store(scm, EcosSystemConnectionMemo.class);
        InstanceManager.getDefault(ShutDownManager.class).deregister(scm.getLocoAddressManager().ecosLocoShutDownTask);
    }

    @AfterEach
    @Override
    public void tearDown() {
        EcosLocoAddressManager em = scm.getLocoAddressManager();
        if ( em != null ) {
            InstanceManager.getDefault(ShutDownManager.class).deregister(em.ecosLocoShutDownTask);
            em.terminateThreads();
            JUnitUtil.waitFor(() -> { return !em.threadsRunning(); });
        }
        
        scm.getTrafficController().terminateThreads();
        scm.dispose();
        JUnitUtil.tearDown();
    }

}
