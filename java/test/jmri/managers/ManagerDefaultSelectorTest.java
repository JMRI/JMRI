package jmri.managers;

import jmri.PowerManager;
import jmri.jmrix.loconet.LnTrafficController;
import jmri.jmrix.loconet.LocoNetInterfaceScaffold;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.jmrix.loconet.SlotManager;
import jmri.profile.Profile;
import jmri.profile.ProfileManager;
import jmri.util.JUnitUtil;
import jmri.util.prefs.InitializationException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test simple functioning of ManagerDefaultSelector
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class ManagerDefaultSelectorTest {

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.resetPreferencesProviders();
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }

    @Ignore("Fails if some yet-to-be-identified test is run beforehand")
    @Test
    public void testIsPreferencesValid() {
        ManagerDefaultSelector mds = new ManagerDefaultSelector();
        // assert default state
        Assert.assertFalse(mds.isAllInternalDefaultsValid());
        Profile profile = ProfileManager.getDefault().getActiveProfile();
        // nothing has been configured, preferences are valid
        Assert.assertTrue(mds.isPreferencesValid(profile));
        try {
            mds.initialize(profile);
        } catch (InitializationException ex) {
            Assert.fail(ex.getMessage());
        }
        // configured with only default Internal connection, preferences are valid
        Assert.assertTrue(mds.isPreferencesValid(profile));
        mds.configure(profile);
        // CMRI provides no known managers, so preferences are valid
        Assert.assertTrue(mds.isPreferencesValid(profile));
        LnTrafficController lnis = new LocoNetInterfaceScaffold();
        SlotManager slotmanager = new SlotManager(lnis);
        LocoNetSystemConnectionMemo loconet = new LocoNetSystemConnectionMemo(lnis, slotmanager);
        mds.configure(profile);
        // LocoNet provides known managers, so preferences are not valid
        // since Internal is the default for all known managers
        Assert.assertFalse(mds.isPreferencesValid(profile));
        mds.setDefault(PowerManager.class, loconet.getUserName());
        // LocoNet now default for a manager, so preferences are valid
        Assert.assertTrue(mds.isPreferencesValid(profile));
        mds.removeConnectionAsDefault(loconet.getUserName());
        // LocoNet provides known managers, so preferences are not valid
        // since Internal is the default for all known managers
        Assert.assertFalse(mds.isPreferencesValid(profile));
        mds.setAllInternalDefaultsValid(true);
        // Internal remains default, LocoNet is present, but overriding, so preferences are valid
        Assert.assertTrue(mds.isPreferencesValid(profile));
        mds.setAllInternalDefaultsValid(false);
        // Internal remains default, LocoNet is present, not overriding, so preferences are not valid
        Assert.assertFalse(mds.isPreferencesValid(profile));
    }

    @Test
    public void testIsAllInternalDefaultsValid() {
        ManagerDefaultSelector mds = new ManagerDefaultSelector();
        // assert default state
        Assert.assertFalse(mds.isAllInternalDefaultsValid());
        mds.setAllInternalDefaultsValid(true);
        // assert set
        Assert.assertTrue(mds.isAllInternalDefaultsValid());
        mds.setAllInternalDefaultsValid(false);
        // assert set again
        Assert.assertFalse(mds.isAllInternalDefaultsValid());
    }

    @Test
    public void testSetAllInternalDefaultsValid() {
        ManagerDefaultSelector mds = new ManagerDefaultSelector();
        // assert default state
        Assert.assertFalse(mds.isAllInternalDefaultsValid());
        mds.setAllInternalDefaultsValid(true);
        // assert set
        Assert.assertTrue(mds.isAllInternalDefaultsValid());
        mds.setAllInternalDefaultsValid(false);
        // assert set again
        Assert.assertFalse(mds.isAllInternalDefaultsValid());
    }

}
