package jmri.jmrit.ctc;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.SignalHead;
import jmri.SignalHeadManager;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.util.JUnitUtil;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.netbeans.jemmy.EventTool;

/**
 * Tests for the CtcRunAction Class
 * @author Dave Sand Copyright (C) 2019
 */
public class CtcRunActionTest {

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Test
    public void testCreate() {
        new CtcRunAction();
    }

    @Test
    public void testAction() {
        // Load the CTC run time
        new CtcRunAction().actionPerformed(null);

        // ** Run time test scenarios **
        InstanceManager im = InstanceManager.getDefault();
        SensorManager sm = im.getDefault(SensorManager.class);
        TurnoutManager tm = im.getDefault(TurnoutManager.class);
        SignalHeadManager hm = im.getDefault(SignalHeadManager.class);

//         hm.getSignalHead("Left-U").addPropertyChangeListener(new java.beans.PropertyChangeListener() {
//             public void propertyChange(java.beans.PropertyChangeEvent evt) {
//                 log.warn(">>>>>>>> : {}", evt);
//             }
//         });

        // Enable debug mode, set the T-Left to normal
//         try {
//             sm.getSensor("IS:DEBUGCTC").setKnownState(Sensor.ACTIVE);
//             sm.getSensor("IS1:SN").setKnownState(Sensor.ACTIVE);
//         } catch (JmriException ex) {
//             log.error("debug exeptions: ", ex);
//         }

        // Clear Left turnout right on main.
        try {
            sm.getSensor("IS1:SN").setKnownState(Sensor.ACTIVE);
            sm.getSensor("IS2:NL").setKnownState(Sensor.INACTIVE);
            sm.getSensor("IS2:RL").setKnownState(Sensor.ACTIVE);
            sm.getSensor("IS2:CB").setKnownState(Sensor.ACTIVE);
            sm.getSensor("IS2:CB").setKnownState(Sensor.INACTIVE);
        } catch (JmriException ex) {
            log.error("sensor exeptions: ", ex);
        }
        new EventTool().waitNoEvent(1000);
        jmri.SignalHead sh = hm.getSignalHead("Left-U");
//         log.warn("Left-U = {} - {}", sh.getHeld(), sh.getAppearanceName());
        Assert.assertFalse(sh.getHeld());  // NOI18N
//         log.warn("Test SHM = {}", hm);
//         log.warn("DEBUG = {}", sm.getSensor("IS:DEBUGCTC").getKnownState());
//         log.warn("SN = {}", sm.getSensor("IS1:SN").getKnownState());
//         log.warn("LL = {}", sm.getSensor("IS2:LL").getKnownState());
//         log.warn("NL = {}", sm.getSensor("IS2:NL").getKnownState());
//         log.warn("RL = {}", sm.getSensor("IS2:RL").getKnownState());
//         log.warn("LK = {}", sm.getSensor("IS2:LK").getKnownState());
//         log.warn("NK = {}", sm.getSensor("IS2:NK").getKnownState());
//         log.warn("RK = {}", sm.getSensor("IS2:RK").getKnownState());
//         jmri.SignalHead sh = hm.getSignalHead("Left-U");
//         log.warn("Left-U = {} - {}", sh.getHeld(), sh.getAppearanceName());
//         sh = hm.getSignalHead("Left-L");
//         log.warn("Left-L = {} - {}", sh.getHeld(), sh.getAppearanceName());
//         sh = hm.getSignalHead("Left-M");
//         log.warn("Left-M = {} - {}", sh.getHeld(), sh.getAppearanceName());
//         sh = hm.getSignalHead("Left-S");
//         log.warn("Left-S = {} - {}", sh.getHeld(), sh.getAppearanceName());
    }

    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        jmri.jmrit.ctc.setup.CreateTestObjects.createTestObjects();
        jmri.jmrit.ctc.setup.CreateTestObjects.createTestFiles();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CtcRunActionTest.class);
}