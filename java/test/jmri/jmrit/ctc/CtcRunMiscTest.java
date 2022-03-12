package jmri.jmrit.ctc;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;

import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.SignalMastManager;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for the CtcRunAction Class.
 *
 * @author Dave Sand Copyright (C) 2020
 */
public class CtcRunMiscTest {

    static final boolean PAUSE = false;

    @Test
    public void testAction() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        // Load the test panel and initialize Logix and advanced block routing
        java.io.File f = new java.io.File("java/test/jmri/jmrit/ctc/configurexml/load/CTC_Test_Misc_Scenarios.xml");  // NOI18N
        InstanceManager.getDefault(jmri.ConfigureManager.class).load(f);
        InstanceManager.getDefault(jmri.LogixManager.class).activateAllLogixs();
        InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class).initializeLayoutBlockPaths();

        SensorManager sm = InstanceManager.getDefault(SensorManager.class);
        JUnitUtil.waitFor(5000);     // Wait for block routing and SML initialization

        // Load the CTC run time
        new CtcRunAction().actionPerformed(null);
        JUnitUtil.waitFor(1000);     // Wait for CTC run time to finish its setup

        // ** Misc run time test scenarios **

        // Do auto code button
        sm.getSensor("IS7:LEVER").setKnownState(Sensor.INACTIVE);
        JUnitUtil.waitFor(()->{return sm.getSensor("IS7:SWRI").getKnownState() == Sensor.ACTIVE;},"7/8 turnout thrown indicator not active");
        Assert.assertTrue(sm.getSensor("IS7:SWRI").getKnownState() == Sensor.ACTIVE);

        sm.getSensor("IS8:RDGL").setKnownState(Sensor.INACTIVE);
        sm.getSensor("IS8:NGL").setKnownState(Sensor.INACTIVE);
        sm.getSensor("IS8:LDGL").setKnownState(Sensor.ACTIVE);
        JUnitUtil.waitFor(()->{return sm.getSensor("IS8:LDGK").getKnownState() == Sensor.ACTIVE;},"7/8 signal left indicator not active");
        Assert.assertTrue(sm.getSensor("IS8:LDGK").getKnownState() == Sensor.ACTIVE);

        if (PAUSE) JUnitUtil.waitFor(2000);

        // Stop JMRI fast clock
        sm.getSensor("ISCLOCKRUNNING").setKnownState(Sensor.INACTIVE);

        // Enable fleeting
        sm.getSensor("IS:FLEETING").setKnownState(Sensor.ACTIVE);

        // Enable debug
        sm.getSensor("IS:DEBUGCTC").setKnownState(Sensor.ACTIVE);

        // Turnout local control
        sm.getSensor("IS6:LOCKTOGGLE").setKnownState(Sensor.ACTIVE);
        sm.getSensor("IS6:CB").setKnownState(Sensor.ACTIVE);
        JUnitUtil.waitFor(()->{return sm.getSensor("IS6:UNLOCKEDINDICATOR").getKnownState() == Sensor.ACTIVE;},"5/6 unlocked indicator not active");
        Assert.assertTrue(sm.getSensor("IS6:UNLOCKEDINDICATOR").getKnownState() == Sensor.ACTIVE);

        if (PAUSE) JUnitUtil.waitFor(2000);

        // Reload run time
        sm.getSensor("IS:RELOADCTC").setKnownState(Sensor.ACTIVE);
        // See if the reload reset the signal indicaotr state
        JUnitUtil.waitFor(()->{return sm.getSensor("IS8:NGK").getKnownState() == Sensor.ACTIVE;},"7/8 normal indicator not active");
        Assert.assertTrue(sm.getSensor("IS8:NGK").getKnownState() == Sensor.ACTIVE);

        if (PAUSE) JUnitUtil.waitFor(5000);

    }

    @BeforeEach
    public void setUp(@TempDir File folder) throws Exception {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager(new jmri.profile.NullProfile(folder));
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initRouteManager();
        JUnitUtil.initDefaultSignalMastManager();
        JUnitUtil.initSignalMastLogicManager();
        JUnitUtil.initLayoutBlockManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.resetWindows(false,false);
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.deregisterEditorManagerShutdownTask();
        JUnitUtil.tearDown();
    }

//     private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CtcRunActionTest.class);
}
