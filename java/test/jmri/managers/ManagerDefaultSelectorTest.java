package jmri.managers;

import jmri.InstanceManager;
import jmri.PowerManager;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.jmrix.loconet.LnCommandStationType;
import jmri.jmrix.loconet.LnTrafficController;
import jmri.jmrix.loconet.LocoNetInterfaceScaffold;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.profile.Profile;
import jmri.profile.ProfileManager;
import jmri.util.JUnitUtil;
import jmri.util.prefs.InitializationException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
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
        JUnitUtil.initInternalSensorManager();  // start proxies, which start internal
        JUnitUtil.initInternalLightManager();  // start proxies, which start internal
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    @Test
    public void testInitialPreferencesValid() throws InitializationException {
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

        // empty profile has defaults for no managers
        Assert.assertTrue(mds.isPreferencesValid(profile));
        Assert.assertEquals("getDefault(ThrottleManager) ", null, mds.getDefault(jmri.ThrottleManager.class));
        Assert.assertEquals("getDefault(LightManager) ", null, mds.getDefault(jmri.LightManager.class)); // not managed

        Assert.assertTrue(mds.isPreferencesValid(profile));

        // configured with only default Internal connection, preferences are valid
        InitializationException ex = mds.configure(profile);
        if (ex != null) {
            throw ex; // bomb out with an error
        }
        Assert.assertTrue(mds.isPreferencesValid(profile));
    }

    private LocoNetSystemConnectionMemo getLocoNetTestConnection() {
        // create a test loconet connection
        LocoNetSystemConnectionMemo memo = new LocoNetSystemConnectionMemo();
        LnTrafficController lnis = new LocoNetInterfaceScaffold(memo);
        memo.setLnTrafficController(lnis);
        memo.configureCommandStation(LnCommandStationType.COMMAND_STATION_DCS100, false, false, false);
        return memo;
    }

    @Test
    public void testSingleSystemPreferencesValid() throws InitializationException {
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

        // add a LocoNet connection
        LocoNetSystemConnectionMemo loconet = getLocoNetTestConnection();

        // wait for notifications
        JUnitUtil.waitFor(() -> {
            return 1 == loconet.getPropertyChangeListeners().length;
        }, "Registration Complete");
        new org.netbeans.jemmy.QueueTool().waitEmpty(20);

        InitializationException ex = mds.configure(profile);
        if (ex != null) {
            throw ex; // bomb out with an error
        }

        // check defaults are 1st hardware system
        Assert.assertEquals("getDefault(ThrottleManager) ", "LocoNet", mds.getDefault(jmri.ThrottleManager.class));
        Assert.assertEquals("getDefault(ConsistManager) ", "LocoNet", mds.getDefault(jmri.ConsistManager.class));
        Assert.assertEquals("getDefault(PowerManager) ", "LocoNet", mds.getDefault(jmri.PowerManager.class));
        Assert.assertEquals("getDefault(GlobalProgrammerManager) ", "LocoNet", mds.getDefault(jmri.GlobalProgrammerManager.class));
        Assert.assertEquals("getDefault(LightManager) ", null, mds.getDefault(jmri.LightManager.class)); // not managed

        // LocoNet provides known managers, so preferences are valid
        Assert.assertTrue(mds.isPreferencesValid(profile));

        mds.setDefault(PowerManager.class, loconet.getUserName());
        Assert.assertEquals("getDefault(PowerManager) ", "LocoNet", mds.getDefault(jmri.PowerManager.class));

        mds.removeConnectionAsDefault(loconet.getUserName());
        Assert.assertEquals("getDefault(PowerManager) ", null, mds.getDefault(jmri.PowerManager.class));

        // loconet gone, auto internal is by itself, so OK
        Assert.assertTrue(mds.isPreferencesValid(profile));

        loconet.dispose();

    }

    @Test
    public void testAuxInternalPreferencesValid() throws InitializationException {
        ManagerDefaultSelector mds = new ManagerDefaultSelector();
        Profile profile = ProfileManager.getDefault().getActiveProfile();

        // nothing has been configured, preferences are valid
        Assert.assertTrue(mds.isPreferencesValid(profile));
        try {
            mds.initialize(profile);
        } catch (InitializationException ex) {
            Assert.fail(ex.getMessage());
        }

        // add a LocoNet connection
        LocoNetSystemConnectionMemo loconet = getLocoNetTestConnection();

        // wait for notifications
        JUnitUtil.waitFor(() -> {
            return 1 == loconet.getPropertyChangeListeners().length;
        }, "Registration Complete");
        new org.netbeans.jemmy.QueueTool().waitEmpty(20);

        // get existing Internal connection
        InternalSystemConnectionMemo internal = InstanceManager.getDefault(InternalSystemConnectionMemo.class); // self registering

        // wait for notifications
        JUnitUtil.waitFor(() -> {
            return 0 < internal.getPropertyChangeListeners().length;
        }, "Registration Complete");
        new org.netbeans.jemmy.QueueTool().waitEmpty(20);

        InitializationException ex = mds.configure(profile);
        if (ex != null) {
            throw ex; // bomb out with an error
        }

        // check defaults are 1st hardware system
        Assert.assertEquals("getDefault(ThrottleManager) ", "LocoNet", mds.getDefault(jmri.ThrottleManager.class));
        Assert.assertEquals("getDefault(ConsistManager) ", "LocoNet", mds.getDefault(jmri.ConsistManager.class));
        Assert.assertEquals("getDefault(PowerManager) ", "LocoNet", mds.getDefault(jmri.PowerManager.class));
        Assert.assertEquals("getDefault(GlobalProgrammerManager) ", "LocoNet", mds.getDefault(jmri.GlobalProgrammerManager.class));
        Assert.assertEquals("getDefault(LightManager) ", null, mds.getDefault(jmri.LightManager.class)); // not managed

        // LocoNet provides known managers, so preferences are valid
        Assert.assertTrue(mds.isPreferencesValid(profile));

        mds.removeConnectionAsDefault(loconet.getUserName());
        Assert.assertEquals("getDefault(PowerManager) ", null, mds.getDefault(jmri.PowerManager.class));

        mds.setDefault(PowerManager.class, loconet.getUserName());
        Assert.assertEquals("getDefault(PowerManager) ", "LocoNet", mds.getDefault(jmri.PowerManager.class));

        mds.setDefault(PowerManager.class, internal.getUserName());
        Assert.assertEquals("getDefault(PowerManager) ", "Internal", mds.getDefault(jmri.PowerManager.class));

        mds.removeConnectionAsDefault(internal.getUserName());
        Assert.assertEquals("getDefault(PowerManager) ", null, mds.getDefault(jmri.PowerManager.class));

        // loconet gone, auto internal is by itself, so OK
        Assert.assertTrue(mds.isPreferencesValid(profile));

        loconet.dispose();
    }

    @Test
    public void testTwoLoconetPreferencesValid() throws InitializationException {
        ManagerDefaultSelector mds = new ManagerDefaultSelector();
        Profile profile = ProfileManager.getDefault().getActiveProfile();

        // nothing has been configured, preferences are valid
        Assert.assertTrue(mds.isPreferencesValid(profile));
        try {
            mds.initialize(profile);
        } catch (InitializationException ex) {
            Assert.fail(ex.getMessage());
        }

        // add a LocoNet connection
        LocoNetSystemConnectionMemo loconet = getLocoNetTestConnection();

        // wait for notifications
        JUnitUtil.waitFor(() -> {
            return 1 == loconet.getPropertyChangeListeners().length;
        }, "Registration Complete");
        new org.netbeans.jemmy.QueueTool().waitEmpty(20);

        // add another LocoNet connection
        LocoNetSystemConnectionMemo loconet2 = getLocoNetTestConnection();

        // wait for notifications
        JUnitUtil.waitFor(() -> {
            return 1 == loconet2.getPropertyChangeListeners().length;
        }, "Registration Complete");
        new org.netbeans.jemmy.QueueTool().waitEmpty(20);

        InitializationException ex = mds.configure(profile);
        if (ex != null) {
            throw ex; // bomb out with an error
        }

        // check defaults are 1st hardware system
        Assert.assertEquals("getDefault(ThrottleManager) ", "LocoNet", mds.getDefault(jmri.ThrottleManager.class));
        Assert.assertEquals("getDefault(ConsistManager) ", "LocoNet", mds.getDefault(jmri.ConsistManager.class));
        Assert.assertEquals("getDefault(PowerManager) ", "LocoNet", mds.getDefault(jmri.PowerManager.class));
        Assert.assertEquals("getDefault(GlobalProgrammerManager) ", "LocoNet", mds.getDefault(jmri.GlobalProgrammerManager.class));
        Assert.assertEquals("getDefault(LightManager) ", null, mds.getDefault(jmri.LightManager.class)); // not managed

        // LocoNet provides known managers, so preferences are valid
        Assert.assertTrue(mds.isPreferencesValid(profile));

        mds.removeConnectionAsDefault(loconet.getUserName());
        Assert.assertEquals("getDefault(PowerManager) ", null, mds.getDefault(jmri.PowerManager.class));

        mds.setDefault(PowerManager.class, loconet2.getUserName());
        Assert.assertEquals("getDefault(PowerManager) ", "LocoNet2", mds.getDefault(jmri.PowerManager.class));

        mds.removeConnectionAsDefault(loconet2.getUserName());
        Assert.assertEquals("getDefault(PowerManager) ", null, mds.getDefault(jmri.PowerManager.class));

        // loconet and loconet2 gone, auto internal is by itself, so OK
        Assert.assertTrue(mds.isPreferencesValid(profile));

        loconet.dispose();
        loconet2.dispose();
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

    @Test
    public void testWriteReadAllInternalOK() throws InitializationException {
        ManagerDefaultSelector mds = new ManagerDefaultSelector();
        Profile profile = ProfileManager.getDefault().getActiveProfile();

        mds.setAllInternalDefaultsValid(true);
        mds.savePreferences(profile);

        mds = new ManagerDefaultSelector();
        Assert.assertFalse(mds.isAllInternalDefaultsValid());

        mds.initialize(profile);
        Assert.assertTrue(mds.isAllInternalDefaultsValid());
    }

}
