package jmri.jmrit.ctc;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;

import jmri.*;
import jmri.jmrit.display.layoutEditor.LayoutBlockManager;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for the CtcRunAction Class.
 *
 * @author Dave Sand Copyright (C) 2020
 */
public class CtcRunWithSignalMastsTest {

    static final boolean PAUSE = false;

    @Test
    @DisabledIfHeadless
    public void testAction() throws JmriException {

        // Load the test panel and initialize Logix and advanced block routing
        boolean loadComplete = ThreadingUtil.runOnGUIwithReturn(() -> {
            File f = new File("java/test/jmri/jmrit/ctc/configurexml/load/CTC_Test_Masts-SML.xml");
            assertTrue(assertDoesNotThrow( () ->
                    InstanceManager.getDefault(ConfigureManager.class).load(f)));
            InstanceManager.getDefault(LogixManager.class).activateAllLogixs();
            InstanceManager.getDefault(LayoutBlockManager.class).initializeLayoutBlockPaths();

            // opens BeanTable frame for debugging
            //new jmri.jmrit.beantable.ListedTableAction().actionPerformed(null);
            return true;
        });
        assertTrue(loadComplete);

        SensorManager sm = InstanceManager.getDefault(SensorManager.class);
        SignalMastManager smm = InstanceManager.getDefault(SignalMastManager.class);
        JUnitUtil.waitFor(5000);     // Wait for block routing and SML initialization

        // Load the CTC run time
        ThreadingUtil.runOnGUI(() -> new CtcRunAction().actionPerformed(null) );
        JUnitUtil.waitFor(1000);     // Wait for CTC run time to finish its setup

        Sensor is2cb = sm.getSensor("IS2:CB"); // CodeButtonInternalSensor
        Sensor is2ngk = sm.getSensor("IS2:NGK"); // SIDI_NormalInternalSensor
        Sensor is2ngl = sm.getSensor("IS2:NGL"); // SIDL_NormalInternalSensor
        Sensor is2ldgl = sm.getSensor("IS2:LDGL"); // SIDL_LeftInternalSensor
        Sensor is2rdgl = sm.getSensor("IS2:RDGL"); // SIDL_RightInternalSensor
        Sensor is2rdgk = sm.getSensor("IS2:RDGK"); // SIDI_RightInternalSensor

        Sensor is3lever = sm.getSensor("IS3:LEVER"); // SWDL_InternalSensor
        Sensor is3swri = sm.getSensor("IS3:SWRI"); // SWDI_ReversedInternalSensor

        Sensor is4cb = sm.getSensor("IS4:CB"); // CodeButtonInternalSensor
        Sensor is4ngl = sm.getSensor("IS4:NGL"); // SIDL_NormalInternalSensor
        Sensor is4ldgk = sm.getSensor("IS4:LDGK"); // SIDI_LeftInternalSensor
        Sensor is4ldgl = sm.getSensor("IS4:LDGL"); // SIDL_LeftInternalSensor
        Sensor is4callon = sm.getSensor("IS4:CALLON");

        Sensor occSensorAlphaLeft = sm.getSensor("S-Alpha-Left");
        Sensor occSensorAlphaSide = sm.getSensor("S-Alpha-Side");

        assertNotNull(is2cb);
        assertNotNull(is2ngk);
        assertNotNull(is2ngl);
        assertNotNull(is2ldgl);
        assertNotNull(is2rdgl);
        assertNotNull(is2rdgk);

        assertNotNull(is3lever);
        assertNotNull(is3swri);

        assertNotNull(is4cb);
        assertNotNull(is4ngl);
        assertNotNull(is4ldgk);
        assertNotNull(is4ldgl);
        assertNotNull(is4callon);

        assertNotNull(occSensorAlphaLeft);
        assertNotNull(occSensorAlphaSide);


        // Make sure the run time is active
        JUnitUtil.waitFor(()-> is2ngk.getKnownState() == Sensor.ACTIVE,"1/2 signal normal indicator not active");

        // ** Run time test scenarios **

        // Clear Left turnout right on main.
        JUnitUtil.setBeanStateAndWait(is2ldgl, Sensor.INACTIVE);
        JUnitUtil.setBeanStateAndWait(is2ngl, Sensor.INACTIVE);
        JUnitUtil.setBeanStateAndWait(is2rdgl, Sensor.ACTIVE);
        JUnitUtil.setBeanStateAndWait(is2cb, Sensor.ACTIVE);

        JUnitUtil.waitFor(()-> is2rdgk.getKnownState() == Sensor.ACTIVE,
                "1/2 signal right indicator not active");
        assertEquals(Sensor.ACTIVE, is2rdgk.getKnownState());
        var mastALA = smm.getSignalMast("SM-Alpha-Left-A");
        assertNotNull(mastALA);
        assertFalse(mastALA.getHeld());

        pauseAndWait(2000, "Completed Clear Left turnout right on main.");

        // Clear Right turnout left on siding using Call On.
        JUnitUtil.setBeanStateAndWait(is3lever, Sensor.INACTIVE);
        JUnitUtil.setBeanStateAndWait(is4cb, Sensor.ACTIVE);
        JUnitUtil.waitFor(()-> is3swri.getKnownState() == Sensor.ACTIVE,
                "3/4 turnout thrown indicator not active");
        assertEquals(Sensor.ACTIVE, is3swri.getKnownState());

        pauseAndWait(2000, "Completed Clear Right turnout left on siding using Call On.");

        // Set block occupied, move signal lever to left traffic, set Call-On lever
        JUnitUtil.setBeanStateAndWait(is4cb, Sensor.INACTIVE);
        JUnitUtil.setBeanStateAndWait(occSensorAlphaSide, Sensor.ACTIVE);
        JUnitUtil.setBeanStateAndWait(is4ngl, Sensor.INACTIVE);
        JUnitUtil.setBeanStateAndWait(is4ldgl, Sensor.ACTIVE);
        JUnitUtil.setBeanStateAndWait(is4callon, Sensor.ACTIVE);
        JUnitUtil.setBeanStateAndWait(is4cb, Sensor.ACTIVE);

        pauseAndWait(2000, "Before 3/4 signal left indicator not active");

        JUnitUtil.waitFor(()-> is4ldgk.getKnownState() == Sensor.ACTIVE,
            "3/4 signal left indicator not active");
        assertEquals(Sensor.ACTIVE, is4ldgk.getKnownState());
        var mastARA = smm.getSignalMast("SM-Alpha-Right-A");
        assertNotNull(mastARA);
        assertFalse(mastARA.getHeld());

        pauseAndWait(2000, "Completed Set block occupied, move signal lever to left traffic, set Call-On lever");

        // Simulate left to right train
        JUnitUtil.setBeanStateAndWait(occSensorAlphaLeft, Sensor.ACTIVE);
        JUnitUtil.waitFor(()-> is2ngk.getKnownState() == Sensor.ACTIVE,"1/2 signal normal indicator not active");
        assertEquals(Sensor.ACTIVE, is2ngk.getKnownState());
        assertTrue(mastALA.getHeld());

        // Cancel left to right call on
        JUnitUtil.setBeanStateAndWait(is4cb, Sensor.INACTIVE);
        JUnitUtil.setBeanStateAndWait(occSensorAlphaSide, Sensor.INACTIVE);

        pauseAndWait(2000, "Completed Simulate left to right train");

        JUnitUtil.closeAllPanels(); // LE Panel
        JUnitUtil.disposeFrame("SML Panel", false, true); // CTC Machine Panel

    }

    private void pauseAndWait(int delay, String text){
        if (PAUSE) {
            log.info("paused, {}", text);
            JUnitUtil.waitFor(delay);
        }
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
        JUnitUtil.initDefaultSignalMastManager();
        JUnitUtil.initSignalMastLogicManager();
        JUnitUtil.initLayoutBlockManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CtcRunWithSignalMastsTest.class);
}
