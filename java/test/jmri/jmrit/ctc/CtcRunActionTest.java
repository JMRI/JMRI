package jmri.jmrit.ctc;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.SignalHeadManager;
import jmri.util.JUnitUtil;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.netbeans.jemmy.EventTool;

/**
 * Tests for the CtcRunAction Class.
 *
 * @author Dave Sand Copyright (C) 2019
 */
public class CtcRunActionTest {

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Rule
    public org.junit.rules.TemporaryFolder folder = new org.junit.rules.TemporaryFolder();

    @Test
    public void testAction() {
        // Load the CTC run time
        new CtcRunAction().actionPerformed(null);

        // ** Run time test scenarios **
        SensorManager sm = InstanceManager.getDefault(SensorManager.class);
        SignalHeadManager hm = InstanceManager.getDefault(SignalHeadManager.class);

        // Clear Left turnout right on main.
        try {
            sm.getSensor("IS1:SN").setKnownState(Sensor.ACTIVE);
            sm.getSensor("IS2:NL").setKnownState(Sensor.INACTIVE);
            sm.getSensor("IS2:RL").setKnownState(Sensor.ACTIVE);
            sm.getSensor("IS2:CB").setKnownState(Sensor.ACTIVE);
            sm.getSensor("IS2:CB").setKnownState(Sensor.INACTIVE);
        } catch (JmriException ex) {
            log.error("sensor exceptions: ", ex);
        }
        new EventTool().waitNoEvent(1000);
        jmri.SignalHead sh = hm.getSignalHead("Left-U");
        Assert.assertFalse(sh.getHeld());  // NOI18N

        // Clear Right turnout left on siding using Call On.
        try {
            sm.getSensor("B-Side").setKnownState(Sensor.ACTIVE);
            sm.getSensor("IS5:LEVER").setKnownState(Sensor.INACTIVE);
            sm.getSensor("IS6:CB").setKnownState(Sensor.ACTIVE);
            sm.getSensor("IS6:CB").setKnownState(Sensor.INACTIVE);
            new EventTool().waitNoEvent(1000);
            sm.getSensor("IS5:SN").setKnownState(Sensor.INACTIVE);
            sm.getSensor("IS5:SR").setKnownState(Sensor.ACTIVE);

            sm.getSensor("IS6:CALLON").setKnownState(Sensor.ACTIVE);
            sm.getSensor("IS6:NL").setKnownState(Sensor.INACTIVE);
            sm.getSensor("IS6:LL").setKnownState(Sensor.ACTIVE);
            sm.getSensor("IS6:CB").setKnownState(Sensor.ACTIVE);
            sm.getSensor("IS6:CB").setKnownState(Sensor.INACTIVE);
        } catch (JmriException ex) {
            log.error("sensor exceptions: ", ex);
        }
        new EventTool().waitNoEvent(1000);
        sh = hm.getSignalHead("Right-L");
        Assert.assertFalse(sh.getHeld());  // NOI18N

//         log.warn("Test SHM = {}", hm);
//         log.warn("DEBUG = {}", sm.getSensor("IS:DEBUGCTC").getKnownState());
//         log.warn("SN = {}", sm.getSensor("IS3:SN").getKnownState());
//         log.warn("SR = {}", sm.getSensor("IS3:SR").getKnownState());
//         log.warn("LL = {}", sm.getSensor("IS4:LL").getKnownState());
//         log.warn("NL = {}", sm.getSensor("IS4:NL").getKnownState());
//         log.warn("RL = {}", sm.getSensor("IS4:RL").getKnownState());
//         log.warn("LK = {}", sm.getSensor("IS4:LK").getKnownState());
//         log.warn("NK = {}", sm.getSensor("IS4:NK").getKnownState());
//         log.warn("RK = {}", sm.getSensor("IS4:RK").getKnownState());
//         sh = hm.getSignalHead("Right-U");
//         log.warn("Right-U = {} - {}", sh.getHeld(), sh.getAppearanceName());
//         sh = hm.getSignalHead("Right-L");
//         log.warn("Right-L = {} - {}", sh.getHeld(), sh.getAppearanceName());
//         sh = hm.getSignalHead("Right-M");
//         log.warn("Right-M = {} - {}", sh.getHeld(), sh.getAppearanceName());
//         sh = hm.getSignalHead("Right-S");
//         log.warn("Right-S = {} - {}", sh.getHeld(), sh.getAppearanceName());
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();

        try {
            JUnitUtil.resetProfileManager(new jmri.profile.NullProfile(folder.newFolder(jmri.profile.Profile.PROFILE)));
        } catch (java.io.IOException ioe) {
            Assert.fail("failed to setup profile for test");
        }

        jmri.jmrit.ctc.setup.CreateTestObjects.createTestObjects();
        jmri.jmrit.ctc.setup.CreateTestObjects.createTestFiles();
    }

    @After
    public void tearDown() {

        // stop any BlockBossLogic threads created
        JUnitUtil.clearBlockBossLogic();

        JUnitUtil.tearDown();
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CtcRunActionTest.class);
}
