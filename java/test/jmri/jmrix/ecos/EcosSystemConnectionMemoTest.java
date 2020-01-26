package jmri.jmrix.ecos;

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
public class EcosSystemConnectionMemoTest  extends jmri.jmrix.SystemConnectionMemoTestBase {

    private EcosSystemConnectionMemo memo = null;

    @Override
    @Test
    public void testProvidesConsistManager(){
       Assert.assertTrue("Provides ConsistManager",scm.provides(jmri.ConsistManager.class));
    }

    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();
        JUnitUtil.initDefaultUserMessagePreferences();
        scm = memo = new jmri.jmrix.ecos.EcosSystemConnectionMemo();
        memo.setEcosTrafficController(new EcosInterfaceScaffold());
        memo.configureManagers();
        memo.getPreferenceManager().setPreferencesLoaded();
        jmri.InstanceManager.store(memo, jmri.jmrix.ecos.EcosSystemConnectionMemo.class);
    }

    @After
    @Override
    public void tearDown() {
        memo.getLocoAddressManager().terminateThreads();
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

}
