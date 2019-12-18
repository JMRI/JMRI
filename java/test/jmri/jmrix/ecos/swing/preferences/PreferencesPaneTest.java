package jmri.jmrix.ecos.swing.preferences;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of PreferencesPane
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class PreferencesPaneTest {

    jmri.jmrix.ecos.EcosSystemConnectionMemo memo = null;

    @Test
    public void testCtor() {
        PreferencesPane action = new PreferencesPane(new jmri.jmrix.ecos.EcosPreferences(memo));
        Assert.assertNotNull("exists", action);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();
        JUnitUtil.initDefaultUserMessagePreferences();
        memo = new jmri.jmrix.ecos.EcosSystemConnectionMemo();

        jmri.InstanceManager.store(memo, jmri.jmrix.ecos.EcosSystemConnectionMemo.class);
    }

    @After
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }
}
