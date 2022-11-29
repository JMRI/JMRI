package jmri.jmrit.ctc;

import java.io.File;
import java.io.IOException;

import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.SignalHeadManager;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for the CtcRunWithSignalHeads Class.
 *
 * @author Dave Sand Copyright (C) 2020
 */
public class CtcRunWithSignalHeadsTest {

    static final boolean PAUSE = false;

    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testAction() throws Exception {

        // Load the test panel and initialize Logix and advanced block routing
        java.io.File f = new java.io.File("java/test/jmri/jmrit/ctc/configurexml/load/CTC_Test_Heads-SSL.xml");  // NOI18N
        InstanceManager.getDefault(jmri.ConfigureManager.class).load(f);
        InstanceManager.getDefault(jmri.LogixManager.class).activateAllLogixs();

        SensorManager sm = InstanceManager.getDefault(SensorManager.class);
        SignalHeadManager shm = InstanceManager.getDefault(SignalHeadManager.class);
        JUnitUtil.waitFor(2000);     // Wait for block routing and SML initialization

        // Load the CTC run time
        new CtcRunAction().actionPerformed(null);
        JUnitUtil.waitFor(1000);     // Wait for CTC run time to finish its setup
        // Make sure the rum time is active
        JUnitUtil.waitFor(()->{return sm.provideSensor("IS2:NGK").getKnownState() == Sensor.ACTIVE;},"1/2 signal normal indicator not active");

        // ** Run time test scenarios **

        // Clear Left turnout right on main.
        sm.provideSensor("IS2:LDGL").setKnownState(Sensor.INACTIVE);
        sm.provideSensor("IS2:NGL").setKnownState(Sensor.INACTIVE);
        sm.provideSensor("IS2:RDGL").setKnownState(Sensor.ACTIVE);
        sm.provideSensor("IS2:CB").setKnownState(Sensor.ACTIVE);

        JUnitUtil.waitFor(()->{return sm.provideSensor("IS2:RDGK").getKnownState() == Sensor.ACTIVE;},"1/2 signal right indicator not active");
        Assert.assertTrue(sm.provideSensor("IS2:RDGK").getKnownState() == Sensor.ACTIVE);
        var shALAU = shm.getSignalHead("SH-Alpha-Left-AU");
        Assertions.assertNotNull(shALAU);
        Assert.assertFalse(shALAU.getHeld());

        if (PAUSE) JUnitUtil.waitFor(2000);

        // Clear Right turnout left on siding using Call On.
        sm.provideSensor("IS3:LEVER").setKnownState(Sensor.INACTIVE);
        sm.provideSensor("IS4:CB").setKnownState(Sensor.ACTIVE);
        JUnitUtil.waitFor(()->{return sm.provideSensor("IS3:SWRI").getKnownState() == Sensor.ACTIVE;},"3/4 turnout thrown indicator not active");
        Assert.assertTrue(sm.provideSensor("IS3:SWRI").getKnownState() == Sensor.ACTIVE);

        if (PAUSE) JUnitUtil.waitFor(2000);

        // Set block occupied, move signal lever to left traffic, set Call-On lever
        sm.provideSensor("IS4:CB").setKnownState(Sensor.INACTIVE);
        sm.provideSensor("S-Alpha-Side").setKnownState(Sensor.ACTIVE);
        sm.provideSensor("IS4:NGL").setKnownState(Sensor.INACTIVE);
        sm.provideSensor("IS4:LDGL").setKnownState(Sensor.ACTIVE);
        sm.provideSensor("IS4:CALLON").setKnownState(Sensor.ACTIVE);
        JUnitUtil.waitFor(()->{return sm.provideSensor("IS4:CALLON").getKnownState() == Sensor.ACTIVE;},"3/4 signal left call on not active");
        JUnitUtil.waitFor(500);     // Unknown need to wait for something to settle down.
        sm.provideSensor("IS4:CB").setKnownState(Sensor.ACTIVE);

        if (PAUSE) JUnitUtil.waitFor(2000);

        JUnitUtil.waitFor(()->{return sm.provideSensor("IS4:LDGK").getKnownState() == Sensor.ACTIVE;},"3/4 signal left indicator not active");
        Assert.assertTrue(sm.provideSensor("IS4:LDGK").getKnownState() == Sensor.ACTIVE);
        var shARAL = shm.getSignalHead("SH-Alpha-Right-AL");
        Assertions.assertNotNull(shARAL);
        Assert.assertFalse(shARAL.getHeld());

        if (PAUSE) JUnitUtil.waitFor(2000);

        // Simulate left to right train
        sm.provideSensor("S-Alpha-Left").setKnownState(Sensor.ACTIVE);
        JUnitUtil.waitFor(()->{return sm.provideSensor("IS2:NGK").getKnownState() == Sensor.ACTIVE;},"1/2 signal normal indicator not active");
        Assert.assertTrue(sm.provideSensor("IS2:NGK").getKnownState() == Sensor.ACTIVE);
        Assert.assertTrue(shALAU.getHeld());

        // Cancel left to right call on
        sm.provideSensor("IS4:CB").setKnownState(Sensor.INACTIVE);
        sm.provideSensor("S-Alpha-Side").setKnownState(Sensor.INACTIVE);

        if (PAUSE) JUnitUtil.waitFor(5000);

    }

    @BeforeEach
    public void setUp(@TempDir File folder) throws IOException {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager(new jmri.profile.NullProfile(folder));
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initRouteManager();
        JUnitUtil.initInternalSignalHeadManager();
        JUnitUtil.initLayoutBlockManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearBlockBossLogicThreads();
        JUnitUtil.resetWindows(false,false);
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

//     private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CtcRunActionTest.class);
}
