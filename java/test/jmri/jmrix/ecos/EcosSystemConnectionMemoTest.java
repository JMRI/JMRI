package jmri.jmrix.ecos;

import jmri.jmrix.SystemConnectionMemoTestBase;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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

    @Before
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
        jmri.InstanceManager.store(scm, EcosSystemConnectionMemo.class);
    }

    @After
    @Override
    public void tearDown() {
        scm.getLocoAddressManager().terminateThreads();
        scm.getTrafficController().terminateThreads();
        scm.dispose();
        JUnitUtil.tearDown();
    }

}
