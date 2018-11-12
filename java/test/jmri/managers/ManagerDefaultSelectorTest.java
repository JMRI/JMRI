package jmri.managers;

import jmri.PowerManager;
import jmri.jmrix.internal.*;
import jmri.jmrix.loconet.*;
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
        JUnitUtil.initInternalSensorManager();  // start proxies, which start internal
        JUnitUtil.initInternalLightManager();  // start proxies, which start internal
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    @Test
    public void testInitialPreferencesValid() {
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
        mds.configure(profile);
        Assert.assertTrue(mds.isPreferencesValid(profile));
     }

    @Test
    public void testSingleSystemPreferencesValid() {
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
        LnTrafficController lnis = new LocoNetInterfaceScaffold();
        SlotManager slotmanager = new SlotManager(lnis);
        slotmanager.setCommandStationType(jmri.jmrix.loconet.LnCommandStationType.COMMAND_STATION_DCS100);
        LocoNetSystemConnectionMemo loconet = new LocoNetSystemConnectionMemo(lnis, slotmanager);
        
        JUnitUtil.waitFor(() -> {return 1 == loconet.getPropertyChangeListeners().length;}, "Registration Complete");
        new org.netbeans.jemmy.QueueTool().waitEmpty(20);
        
        mds.configure(profile);

        // check defaults check (have to wait for notifications)
        Assert.assertEquals("getDefault(ThrottleManager) ",         "LocoNet",  mds.getDefault(jmri.ThrottleManager.class));
        Assert.assertEquals("getDefault(ConsistManager) ",          "LocoNet",  mds.getDefault(jmri.ConsistManager.class));
        Assert.assertEquals("getDefault(PowerManager) ",            "LocoNet",  mds.getDefault(jmri.PowerManager.class));
        Assert.assertEquals("getDefault(GlobalProgrammerManager) ", "LocoNet",  mds.getDefault(jmri.GlobalProgrammerManager.class));
        Assert.assertEquals("getDefault(LightManager) ",            null,       mds.getDefault(jmri.LightManager.class)); // not managed

        // LocoNet provides known managers, so preferences are valid
        Assert.assertTrue(mds.isPreferencesValid(profile));
                
        mds.setDefault(PowerManager.class, loconet.getUserName());
        Assert.assertEquals("getDefault(PowerManager) ", "LocoNet", mds.getDefault(jmri.PowerManager.class));

        mds.removeConnectionAsDefault(loconet.getUserName());
        Assert.assertEquals("getDefault(PowerManager) ", null, mds.getDefault(jmri.PowerManager.class));

        // loconet gone, auto internal is by itself, so OK
        Assert.assertTrue(mds.isPreferencesValid(profile));
        
    }

    @Test
    public void testAuxInternalPreferencesValid() {
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
        LnTrafficController lnis = new LocoNetInterfaceScaffold();
        SlotManager slotmanager = new SlotManager(lnis);
        slotmanager.setCommandStationType(jmri.jmrix.loconet.LnCommandStationType.COMMAND_STATION_DCS100);
        LocoNetSystemConnectionMemo loconet = new LocoNetSystemConnectionMemo(lnis, slotmanager);

        JUnitUtil.waitFor(() -> {return 1 == loconet.getPropertyChangeListeners().length;}, "Registration Complete");
        new org.netbeans.jemmy.QueueTool().waitEmpty(20);
        
        // add Internal as a second connection
        InternalSystemConnectionMemo internal = new InternalSystemConnectionMemo(false); // self registering

        JUnitUtil.waitFor(() -> {return 1 == internal.getPropertyChangeListeners().length;}, "Registration Complete");
        new org.netbeans.jemmy.QueueTool().waitEmpty(20);
        
        mds.configure(profile);        

        // check defaults check (have to wait for notifications)
        JUnitUtil.waitFor(() -> {return mds.getDefault(jmri.ThrottleManager.class).equals("Internal");}, "default not set");  
              
        Assert.assertEquals("getDefault(ThrottleManager) ",         "Internal", mds.getDefault(jmri.ThrottleManager.class));
        Assert.assertEquals("getDefault(ConsistManager) ",          "Internal", mds.getDefault(jmri.ConsistManager.class));
        Assert.assertEquals("getDefault(PowerManager) ",            "Internal", mds.getDefault(jmri.PowerManager.class));
        Assert.assertEquals("getDefault(GlobalProgrammerManager) ", "Internal", mds.getDefault(jmri.GlobalProgrammerManager.class));
        Assert.assertEquals("getDefault(LightManager) ",            null,       mds.getDefault(jmri.LightManager.class)); // not managed

        // LocoNet provides known managers, so preferences are valid
        Assert.assertTrue(mds.isPreferencesValid(profile));
        
        mds.setDefault(PowerManager.class, loconet.getUserName());
        Assert.assertEquals("getDefault(PowerManager) ", "LocoNet", mds.getDefault(jmri.PowerManager.class));

        mds.setDefault(PowerManager.class, internal.getUserName());
        Assert.assertEquals("getDefault(PowerManager) ", "Internal", mds.getDefault(jmri.PowerManager.class));

        mds.removeConnectionAsDefault(loconet.getUserName());
        Assert.assertEquals("getDefault(PowerManager) ", "Internal", mds.getDefault(jmri.PowerManager.class));

        mds.removeConnectionAsDefault(internal.getUserName());
        Assert.assertEquals("getDefault(PowerManager) ", null, mds.getDefault(jmri.PowerManager.class));

        // loconet gone, auto internal is by itself, so OK
        Assert.assertTrue(mds.isPreferencesValid(profile));
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
