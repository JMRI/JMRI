package jmri.managers;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jmri.ConsistManager;
import jmri.GlobalProgrammerManager;
import jmri.InstanceManager;
import jmri.LightManager;
import jmri.PowerManager;
import jmri.ThrottleManager;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.jmrix.loconet.LnCommandStationType;
import jmri.jmrix.loconet.LnTrafficController;
import jmri.jmrix.loconet.LocoNetInterfaceScaffold;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.profile.Profile;
import jmri.profile.ProfileManager;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;
import jmri.util.prefs.InitializationException;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of ManagerDefaultSelector
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class ManagerDefaultSelectorTest {

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.resetPreferencesProviders();
        JUnitUtil.initInternalSensorManager();  // start proxies, which start internal
        JUnitUtil.initInternalLightManager();  // start proxies, which start internal
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    @Test
    public void testInitialPreferencesValid() throws InitializationException {
        ManagerDefaultSelector mds = new ManagerDefaultSelector();
        // assert default state
        assertFalse(mds.isAllInternalDefaultsValid());
        Profile profile = ProfileManager.getDefault().getActiveProfile();
        // nothing has been configured, preferences are valid
        assertTrue(mds.isPreferencesValid(profile));
        assertDoesNotThrow( () -> mds.initialize(profile));
        

        // empty profile has defaults for no managers
        assertTrue( mds.isPreferencesValid(profile));
        assertNull( mds.getDefault(ThrottleManager.class), "getDefault(ThrottleManager) ");
        assertNull( mds.getDefault(LightManager.class), "getDefault(LightManager) not managed");

        assertTrue(mds.isPreferencesValid(profile));

        // configured with only default Internal connection, preferences are valid
        InitializationException ex = mds.configure(profile);
        if (ex != null) {
            throw ex; // bomb out with an error
        }
        assertTrue(mds.isPreferencesValid(profile));
    }

    private LocoNetSystemConnectionMemo getLocoNetTestConnection() {
        // create a test loconet connection
        LocoNetSystemConnectionMemo memo = new LocoNetSystemConnectionMemo();
        LnTrafficController lnis = new LocoNetInterfaceScaffold(memo);
        memo.setLnTrafficController(lnis);
        memo.configureCommandStation(LnCommandStationType.COMMAND_STATION_DCS100, false, false, false, false, false);
        memo.configureManagers();
        return memo;
    }

    @Test
    @DisabledIfHeadless
    public void testSingleSystemPreferencesValid() throws InitializationException {

        ManagerDefaultSelector mds = new ManagerDefaultSelector();
        // assert default state
        assertFalse(mds.isAllInternalDefaultsValid());
        Profile profile = ProfileManager.getDefault().getActiveProfile();
        // nothing has been configured, preferences are valid
        assertTrue(mds.isPreferencesValid(profile));
        assertDoesNotThrow( () -> mds.initialize(profile));

        // add a LocoNet connection
        LocoNetSystemConnectionMemo loconet = getLocoNetTestConnection();

        // wait for notifications
        JUnitUtil.waitFor(() -> {
            return 2 == loconet.getPropertyChangeListeners().length; // 1 in ManagerDefaultSelector + 1 in AbstractTurnoutManager
        }, "Registration Complete");
        new org.netbeans.jemmy.QueueTool().waitEmpty(20);

        InitializationException ex = mds.configure(profile);
        if (ex != null) {
            throw ex; // bomb out with an error
        }

        // check defaults are 1st hardware system
        assertEquals( "LocoNet", mds.getDefault(ThrottleManager.class), "getDefault(ThrottleManager) ");
        assertEquals( "LocoNet", mds.getDefault(ConsistManager.class), "getDefault(ConsistManager) ");
        assertEquals( "LocoNet", mds.getDefault(PowerManager.class), "getDefault(PowerManager) ");
        assertEquals( "LocoNet", mds.getDefault(GlobalProgrammerManager.class), "getDefault(GlobalProgrammerManager) ");
        assertNull( mds.getDefault(LightManager.class), "getDefault(LightManager) "); // not managed

        // LocoNet provides known managers, so preferences are valid
        assertTrue(mds.isPreferencesValid(profile));

        mds.setDefault(PowerManager.class, loconet.getUserName());
        assertEquals( "LocoNet", mds.getDefault(PowerManager.class), "getDefault(PowerManager) ");

        mds.removeConnectionAsDefault(loconet.getUserName());
        assertNull( mds.getDefault(PowerManager.class), "getDefault(PowerManager) ");

        // loconet gone, auto internal is by itself, so OK
        assertTrue(mds.isPreferencesValid(profile));

        loconet.getPowerManager().dispose();
        loconet.getSensorManager().dispose();
        loconet.dispose();

    }

    @Test
    @DisabledIfHeadless
    public void testAuxInternalPreferencesValid() throws InitializationException {

        ManagerDefaultSelector mds = new ManagerDefaultSelector();
        Profile profile = ProfileManager.getDefault().getActiveProfile();

        // nothing has been configured, preferences are valid
        assertTrue(mds.isPreferencesValid(profile));
        assertDoesNotThrow( () -> mds.initialize(profile));

        // add a LocoNet connection
        LocoNetSystemConnectionMemo loconet = getLocoNetTestConnection();

        // wait for notifications
        JUnitUtil.waitFor(() -> {
            return 2 == loconet.getPropertyChangeListeners().length; // 1 in ManagerDefaultSelector + 1 in AbstractTurnoutManager
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
        assertEquals( "LocoNet", mds.getDefault(ThrottleManager.class), "getDefault(ThrottleManager) ");
        assertEquals( "LocoNet", mds.getDefault(ConsistManager.class), "getDefault(ConsistManager) ");
        assertEquals( "LocoNet", mds.getDefault(PowerManager.class), "getDefault(PowerManager) ");
        assertEquals( "LocoNet", mds.getDefault(GlobalProgrammerManager.class), "getDefault(GlobalProgrammerManager) ");
        assertNull( mds.getDefault(LightManager.class), "getDefault(LightManager) "); // not managed

        // LocoNet provides known managers, so preferences are valid
        assertTrue(mds.isPreferencesValid(profile));

        mds.removeConnectionAsDefault(loconet.getUserName());
        assertNull(  mds.getDefault(PowerManager.class), "getDefault(PowerManager) ");

        mds.setDefault(PowerManager.class, loconet.getUserName());
        assertEquals( "LocoNet", mds.getDefault(PowerManager.class), "getDefault(PowerManager) ");

        mds.setDefault(PowerManager.class, internal.getUserName());
        assertEquals( "Internal", mds.getDefault(PowerManager.class), "getDefault(PowerManager) ");

        mds.removeConnectionAsDefault(internal.getUserName());
        assertNull( mds.getDefault(PowerManager.class), "getDefault(PowerManager) ");

        // loconet gone, auto internal is by itself, so OK
        assertTrue(mds.isPreferencesValid(profile));

        loconet.getPowerManager().dispose();
        loconet.getSensorManager().dispose();
        loconet.dispose();
    }

    @Test
    @DisabledIfHeadless
    public void testTwoLoconetPreferencesValid() throws InitializationException {

        ManagerDefaultSelector mds = new ManagerDefaultSelector();
        Profile profile = ProfileManager.getDefault().getActiveProfile();

        // nothing has been configured, preferences are valid
        assertTrue(mds.isPreferencesValid(profile));
        assertDoesNotThrow( () -> mds.initialize(profile));

        // add a LocoNet connection
        LocoNetSystemConnectionMemo loconet = getLocoNetTestConnection();

        // wait for notifications
        JUnitUtil.waitFor(() -> {
            return 2 == loconet.getPropertyChangeListeners().length; // 1 in ManagerDefaultSelector + 1 in AbstractTurnoutManager
        }, "Registration Complete");
        new org.netbeans.jemmy.QueueTool().waitEmpty(20);

        // add another LocoNet connection
        LocoNetSystemConnectionMemo loconet2 = getLocoNetTestConnection();

        // wait for notifications
        JUnitUtil.waitFor(() -> {
            return 2 == loconet2.getPropertyChangeListeners().length; // 1 in ManagerDefaultSelector + 1 in AbstractTurnoutManager
        }, "Registration Complete");
        new org.netbeans.jemmy.QueueTool().waitEmpty(20);

        InitializationException ex = mds.configure(profile);
        if (ex != null) {
            throw ex; // bomb out with an error
        }

        // check defaults are 1st hardware system
        assertEquals( "LocoNet", mds.getDefault(ThrottleManager.class), "getDefault(ThrottleManager) ");
        assertEquals( "LocoNet", mds.getDefault(ConsistManager.class), "getDefault(ConsistManager) ");
        assertEquals( "LocoNet", mds.getDefault(PowerManager.class), "getDefault(PowerManager) ");
        assertEquals( "LocoNet", mds.getDefault(GlobalProgrammerManager.class), "getDefault(GlobalProgrammerManager) ");
        assertNull( mds.getDefault(LightManager.class), "getDefault(LightManager) "); // not managed

        // LocoNet provides known managers, so preferences are valid
        assertTrue(mds.isPreferencesValid(profile));

        mds.removeConnectionAsDefault(loconet.getUserName());
        assertNull( mds.getDefault(PowerManager.class), "getDefault(PowerManager) ");

        mds.setDefault(PowerManager.class, loconet2.getUserName());
        assertEquals( "LocoNet2", mds.getDefault(PowerManager.class), "getDefault(PowerManager) ");

        mds.removeConnectionAsDefault(loconet2.getUserName());
        assertNull( mds.getDefault(PowerManager.class), "getDefault(PowerManager) ");

        // loconet and loconet2 gone, auto internal is by itself, so OK
        assertTrue(mds.isPreferencesValid(profile));

        loconet.getPowerManager().dispose();
        loconet.getSensorManager().dispose();
        loconet.dispose();

        loconet2.getPowerManager().dispose();
        loconet2.getSensorManager().dispose();
        loconet2.dispose();
    }

    @Test
    public void testSetAllInternalDefaultsValid() {
        ManagerDefaultSelector mds = new ManagerDefaultSelector();
        // assert default state
        assertFalse(mds.isAllInternalDefaultsValid());
        mds.setAllInternalDefaultsValid(true);
        // assert set
        assertTrue(mds.isAllInternalDefaultsValid());
        mds.setAllInternalDefaultsValid(false);
        // assert set again
        assertFalse(mds.isAllInternalDefaultsValid());
    }

    @Test
    public void testWriteReadAllInternalOK() throws InitializationException {
        ManagerDefaultSelector mds = new ManagerDefaultSelector();
        Profile profile = ProfileManager.getDefault().getActiveProfile();

        mds.setAllInternalDefaultsValid(true);
        mds.savePreferences(profile);

        mds = new ManagerDefaultSelector();
        assertFalse(mds.isAllInternalDefaultsValid());

        mds.initialize(profile);
        assertTrue(mds.isAllInternalDefaultsValid());
    }

}
