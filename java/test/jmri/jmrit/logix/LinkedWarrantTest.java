package jmri.jmrit.logix;

import java.io.File;
import java.util.List;
import java.util.ArrayList;
import jmri.*;
import jmri.jmrit.display.controlPanelEditor.ControlPanelEditor;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.netbeans.jemmy.operators.JFrameOperator;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Tests for running multiple Warrants
 *
 * @author  Pete Cressman 2015
 */
@Timeout(60)
@DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
@DisabledIfSystemProperty(named ="jmri.skipTestsRequiringSeparateRunning", matches ="true")
public class LinkedWarrantTest {

    private OBlockManager _OBlockMgr;
    private SensorManager _sensorMgr;
    private WarrantManager _warrantMgr;

    // tests a warrant launching itself. (origin, destination the same to make continuous loop)
    @Test
    public void testLoopedWarrant() throws Exception {
        // load and display
        File f = new File("java/test/jmri/jmrit/logix/valid/ShortBlocksTest.xml");
        InstanceManager.getDefault(ConfigureManager.class).load(f);
        jmri.util.JUnitAppender.suppressErrorMessage("Portal elem = null");

        ControlPanelEditor panel = (ControlPanelEditor) jmri.util.JmriJFrame.getFrame("LinkedWarrantsTest");

        Sensor sensor1 = _sensorMgr.getBySystemName("IS12");
        assertThat(sensor1).withFailMessage("Senor IS12 not found").isNotNull();
        NXFrameTest.setAndConfirmSensorAction(sensor1, Sensor.ACTIVE, _OBlockMgr.getBySystemName("OB12"));

        WarrantTableFrame tableFrame = WarrantTableFrame.getDefault();
        assertThat(tableFrame).withFailMessage("tableFrame").isNotNull();

        Warrant warrant = _warrantMgr.getWarrant("LoopDeLoop");
        assertThat(warrant).withFailMessage("warrant").isNotNull();

        // OBlock of route
        String[] route = {"OB12", "OB1", "OB3", "OB5", "OB6", "OB7", "OB9", "OB11", "OB12"};
        OBlock block = _OBlockMgr.getOBlock("OB12");

        // WarrantTable.runTrain() returns a string that is not null if the
        // warrant can't be started
        assertThat(tableFrame.runTrain(warrant, Warrant.MODE_RUN)).withFailMessage("Warrant starts").isNull(); // start run

        jmri.util.JUnitUtil.waitFor(() -> {
            String m =  warrant.getRunningMessage();
            return m.endsWith("Cmd #8.");
        }, "Loopy 1 starts to move at 8th command");

        // Run the train, then checks end location
        assertThat(NXFrameTest.runtimes(route, _OBlockMgr).getDisplayName()).withFailMessage("LoopDeLoop after first leg").isEqualTo(block.getSensor().getDisplayName());
        // It takes 600+ milliseconds per block to execute NXFrameTest.runtimes()
        // i.e. wait at least 600 * route.length for return

        jmri.util.JUnitUtil.waitFor(() -> {
            String m = tableFrame.getStatus();
            return m.startsWith("Warrant");
        }, "LoopDeLoop finished first leg");

        jmri.util.JUnitUtil.waitFor(() -> {
            String m =  warrant.getRunningMessage();
            return m.endsWith("Cmd #8.");
        }, "Loopy 2 starts to move at 8th command");

        assertThat(NXFrameTest.runtimes(route, _OBlockMgr).getDisplayName()).withFailMessage("LoopDeLoop after second leg").isEqualTo(block.getSensor().getDisplayName());

        jmri.util.JUnitUtil.waitFor(() -> {
            String m = tableFrame.getStatus();
            return m.startsWith("Warrant");
        }, "LoopDeLoop finished second leg");

        jmri.util.JUnitUtil.waitFor(() -> {
            String m =  warrant.getRunningMessage();
            return m.endsWith("Cmd #8.");
        }, "Loopy 3 starts to move at 8th command");

        assertThat(NXFrameTest.runtimes(route, _OBlockMgr).getDisplayName()).withFailMessage("LoopDeLoop after last leg").isEqualTo(block.getSensor().getDisplayName());

        // passed test - cleanup.  Do it here so failure leaves traces.
        JFrameOperator jfo = new JFrameOperator(tableFrame);
        jfo.requestClose();
        // we may want to use jemmy to close the panel as well.
        assert panel != null;
        panel.dispose();    // disposing this way allows test to be rerun (i.e. reload panel file) multiple times
    }

    // Tests warrant launching a different warrant with different address. Origin location cannot be destination of the other)
    @Test
    public void testLinkedWarrant() throws Exception {
        // load and display
        File f = new File("java/test/jmri/jmrit/logix/valid/ShortBlocksTest.xml");
        InstanceManager.getDefault(ConfigureManager.class).load(f);
        jmri.util.JUnitAppender.suppressErrorMessage("Portal elem = null");

        WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);

        ControlPanelEditor panel = (ControlPanelEditor) jmri.util.JmriJFrame.getFrame("LinkedWarrantsTest");
//        panel.setVisible(false);  // hide panel to prevent repaint.

        final Sensor sensor12 = _sensorMgr.getBySystemName("IS12");
        assertThat(sensor12).withFailMessage("Sensor IS12 not found").isNotNull();

        Sensor sensor1 = _sensorMgr.getBySystemName("IS1");
        assertThat(sensor1).withFailMessage("Senor IS1 not found").isNotNull();
        NXFrameTest.setAndConfirmSensorAction(sensor12, Sensor.ACTIVE, _OBlockMgr.getBySystemName("OB12"));

        WarrantTableFrame tableFrame = WarrantTableFrame.getDefault();
        assertThat(tableFrame).withFailMessage("tableFrame").isNotNull();

        Warrant warrant = _warrantMgr.getWarrant("Loop&Fred");
        assertThat(warrant).withFailMessage("warrant").isNotNull();

        // WarrantTable.runTrain() returns a string that is not null if the
        // warrant can't be started
        assertThat(tableFrame.runTrain(warrant, Warrant.MODE_RUN)).withFailMessage("Warrant starts").isNull(); // start run

        Warrant w = warrant;
        jmri.util.JUnitUtil.waitFor(() -> {
            String m =  w.getRunningMessage();
            return m.endsWith("Cmd #8.");
        }, "Train starts to move at 8th command");

       // OBlock of route
        String[] route1 = {"OB12", "OB1", "OB3", "OB5", "OB6", "OB7", "OB9", "OB11", "OB12"};
        OBlock block = _OBlockMgr.getOBlock("OB12");

        // Run the train, then checks end location
        assertThat(NXFrameTest.runtimes(route1, _OBlockMgr).getDisplayName()).withFailMessage("Train after first leg").isEqualTo(block.getSensor().getDisplayName());
        // It takes 600+ milliseconds per block to execute NXFrameTest.runtimes()
        
        // "Loop&Fred" links to "WestToEast". Get start for "WestToEast" occupied quickly
        NXFrameTest.setAndConfirmSensorAction(sensor1, Sensor.ACTIVE, _OBlockMgr.getBySystemName("OB1"));

        warrant = _warrantMgr.getWarrant("WestToEast");

        Warrant ww = warrant;
        jmri.util.JUnitUtil.waitFor(() -> {
            String m =  ww.getRunningMessage();
            return m.endsWith("Cmd #8.");
        }, "Train Fred starts to move at 8th command");

        String[] route2 = {"OB1", "OB3", "OB5", "OB6", "OB7", "OB9", "OB11"};
        block = _OBlockMgr.getOBlock("OB11");

        assertThat(NXFrameTest.runtimes(route2, _OBlockMgr).getDisplayName()).withFailMessage("Train after second leg").isEqualTo(block.getSensor().getDisplayName());

        // passed test - cleanup.  Do it here so failure leaves traces.
        JFrameOperator jfo = new JFrameOperator(tableFrame);
        jfo.requestClose();
        // we may want to use jemmy to close the panel as well.
        assert panel != null;
        panel.dispose();    // disposing this way allows test to be rerun (i.e. reload panel file) multiple times
    }

    // tests a warrant running a train out and launching a return train
    // Both warrants have the same address and origin of each is destination of the other
    @Test
    public void testBackAndForth() throws Exception {
        // load and display
        File f = new File("java/test/jmri/jmrit/logix/valid/ShortBlocksTest.xml");
        InstanceManager.getDefault(ConfigureManager.class).load(f);
        jmri.util.JUnitAppender.suppressErrorMessage("Portal elem = null");

        WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);

        ControlPanelEditor panel = (ControlPanelEditor) jmri.util.JmriJFrame.getFrame("LinkedWarrantsTest");
//        panel.setVisible(false);  // hide panel to prevent repaint.

        final Sensor sensor1 = _sensorMgr.getBySystemName("IS1");
        assertThat(sensor1).withFailMessage("Senor IS1 not found").isNotNull();

        NXFrameTest.setAndConfirmSensorAction(sensor1, Sensor.ACTIVE, _OBlockMgr.getBySystemName("OB1"));

        WarrantTableFrame tableFrame = WarrantTableFrame.getDefault();
        assertThat(tableFrame).withFailMessage("tableFrame").isNotNull();

        Warrant outWarrant = _warrantMgr.getWarrant("WestToEastLink");
        assertThat(outWarrant).withFailMessage("WestWarrant").isNotNull();
        Warrant backWarrant = _warrantMgr.getWarrant("EastToWestLink");

        // OBlock of route
        String[] routeOut = {"OB1", "OB3", "OB5", "OB6", "OB7", "OB9", "OB11"};
        String outEndSensorName = _OBlockMgr.getOBlock("OB11").getSensor().getDisplayName();
        String[] routeBack = {"OB11", "OB9", "OB7", "OB6", "OB5", "OB3", "OB1"};
        String backEndSensorName = _OBlockMgr.getOBlock("OB1").getSensor().getDisplayName();

        // WarrantTable.runTrain() returns a string that is not null if the
        // warrant can't be started
        assertThat(tableFrame.runTrain(outWarrant, Warrant.MODE_RUN)).withFailMessage("Warrant starts").isNull(); // start run

        jmri.util.JUnitUtil.waitFor(() -> {
            String m =  outWarrant.getRunningMessage();
            return m.endsWith("Cmd #8.");
        }, "WestToEastLink starts to move at 8th command");

        // Run the train, then checks end location
        assertThat(NXFrameTest.runtimes(routeOut, _OBlockMgr).getDisplayName()).withFailMessage("Train after first leg").isEqualTo(outEndSensorName);
        // It takes 600+ milliseconds per block to execute NXFrameTest.runtimes()
        // i.e. wait at least 600 * (route.length - 1) for return
        
        jmri.util.JUnitUtil.waitFor(() -> {
            String m = tableFrame.getStatus();
            return m.startsWith("Warrant");
        }, "WestToEastLink finished first leg out");

        jmri.util.JUnitUtil.waitFor(() -> {
            String m =  backWarrant.getRunningMessage();
            return m.endsWith("Cmd #8.");
        }, "EastToWestLink starts to move at 8th command");

        assertThat(NXFrameTest.runtimes(routeBack, _OBlockMgr).getDisplayName()).withFailMessage("Train after second leg").isEqualTo(backEndSensorName);
        // It takes 600+ milliseconds per block to execute NXFrameTest.runtimes()

        jmri.util.JUnitUtil.waitFor(() -> {
            String m = tableFrame.getStatus();
            return m.startsWith("Warrant");
        }, "EastToWestLink finished second leg back");

        jmri.util.JUnitUtil.waitFor(() -> {
            String m =  outWarrant.getRunningMessage();
            return m.endsWith("Cmd #8.");
        }, "WestToEastLink starts to move at 8th command");

        assertThat(NXFrameTest.runtimes(routeOut, _OBlockMgr).getDisplayName()).withFailMessage("Train after third leg").isEqualTo(outEndSensorName);
        // It takes 600+ milliseconds per block to execute NXFrameTest.runtimes()
 
        jmri.util.JUnitUtil.waitFor(() -> {
            String m = tableFrame.getStatus();
            return m.startsWith("Warrant");
        }, "WestToEastLink finished third leg");

        jmri.util.JUnitUtil.waitFor(() -> {
            String m =  backWarrant.getRunningMessage();
            return m.endsWith("Cmd #8.");
        }, "EastToWestLink starts to move at 8th command");

        assertThat(NXFrameTest.runtimes(routeBack, _OBlockMgr).getDisplayName()).withFailMessage("Train after fourth leg").isEqualTo(backEndSensorName);
        // It takes 600+ milliseconds per block to execute NXFrameTest.runtimes()

            // passed test - cleanup.  Do it here so failure leaves traces.
            JFrameOperator jfo = new JFrameOperator(tableFrame);
            jfo.requestClose();
            // we may want to use jemmy to close the panel as well.
        assert panel != null;
        panel.dispose();    // disposing this way allows test to be rerun (i.e. reload panel file) multiple times
    }

    // Tests warrant launching 3 different warrants mid script - tinker to Evers to Chance (1910 Chicago Cubs)
    @Test
    public void testLinkedMidScript() throws Exception {
        // load and display
        File f = new File("java/test/jmri/jmrit/logix/valid/NXWarrantTest.xml");
        InstanceManager.getDefault(ConfigureManager.class).load(f);
        jmri.util.JUnitAppender.suppressErrorMessage("Portal elem = null");

        WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);

        ControlPanelEditor panel = (ControlPanelEditor) jmri.util.JmriJFrame.getFrame("NXWarrantTest");
//        panel.setVisible(false);  // hide panel to prevent repaint.

        // Tinker start block
        Sensor sensor0 = _sensorMgr.getBySystemName("IS0");
        assertThat(sensor0).withFailMessage("Senor IS0 not found").isNotNull();
        NXFrameTest.setAndConfirmSensorAction(sensor0, Sensor.ACTIVE, _OBlockMgr.getBySystemName("OB0"));

        // Evers start block
        Sensor sensor7 = _sensorMgr.getBySystemName("IS7");
        assertThat(sensor7).withFailMessage("Senor IS7 not found").isNotNull();
        NXFrameTest.setAndConfirmSensorAction(sensor7, Sensor.ACTIVE, _OBlockMgr.getBySystemName("OB7"));

        // Chance start block
        Sensor sensor6 = _sensorMgr.getBySystemName("IS6");
        assertThat(sensor6).withFailMessage("Senor IS6 not found").isNotNull();
        NXFrameTest.setAndConfirmSensorAction(sensor6, Sensor.ACTIVE, _OBlockMgr.getBySystemName("OB6"));

        WarrantTableFrame tableFrame = WarrantTableFrame.getDefault();
        assertThat(tableFrame).withFailMessage("tableFrame").isNotNull();

        Warrant w = _warrantMgr.getWarrant("Tinker");
        assertThat(w).withFailMessage("warrant").isNotNull();

        // WarrantTable.runTrain() returns a string that is not null if the
        // warrant can't be started
        assertThat(tableFrame.runTrain(w, Warrant.MODE_RUN)).withFailMessage("Warrant starts").isNull(); // start run

        jmri.util.JUnitUtil.waitFor(() -> {
            String m =  w.getRunningMessage();
            return m.endsWith("Cmd #8.");
        }, "Tinker starts to move at 8th command");

       // OBlock of route
        String[] route1 = {"OB0", "OB1", "OB2", "OB3", "OB4", "OB5", "OB10"};
        OBlock block = _OBlockMgr.getOBlock("OB10");

        // Run the train, then checks end location
        assertThat(NXFrameTest.runtimes(route1, _OBlockMgr).getDisplayName()).withFailMessage("Tinker after first leg").isEqualTo(block.getSensor().getDisplayName());
        // It takes 600+ milliseconds per block to execute NXFrameTest.runtimes()
        // i.e. wait at least 600 * route.length for return

        Warrant ww = _warrantMgr.getWarrant("Evers");

        jmri.util.JUnitUtil.waitFor(() -> {
            String m =  ww.getRunningMessage();
            return m.endsWith("Cmd #8.");
        }, "Evers starts to move at 8th command");

        String[] route2 = {"OB7", "OB3", "OB2", "OB1"};
        block = _OBlockMgr.getOBlock("OB1");

        assertThat(NXFrameTest.runtimes(route2, _OBlockMgr).getDisplayName()).withFailMessage("Evers after second leg").isEqualTo(block.getSensor().getDisplayName());
        // It takes 600+ milliseconds per block to execute NXFrameTest.runtimes()

        Warrant www = _warrantMgr.getWarrant("Chance");

        jmri.util.JUnitUtil.waitFor(() -> {
            String m =  www.getRunningMessage();
            return m.endsWith("Cmd #8.");
        }, "Chance starts to move at 8th command");

        String[] route3 = {"OB6", "OB3", "OB4", "OB5"};
        block = _OBlockMgr.getOBlock("OB5");

        assertThat(NXFrameTest.runtimes(route3, _OBlockMgr).getDisplayName()).withFailMessage("Chance after third leg").isEqualTo(block.getSensor().getDisplayName());
        // It takes 600+ milliseconds per block to execute NXFrameTest.runtimes()

        // passed test - cleanup.  Do it here so failure leaves traces.
        JFrameOperator jfo = new JFrameOperator(tableFrame);
        jfo.requestClose();
        // we may want to use jemmy to close the panel as well.
        assert panel != null;
        panel.dispose();    // disposing this way allows test to be rerun (i.e. reload panel file) multiple times
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalSignalHeadManager();
        JUnitUtil.initLayoutBlockManager();
        JUnitUtil.initDebugPowerManager();
        JUnitUtil.initDebugThrottleManager();
        JUnitUtil.initMemoryManager();
        JUnitUtil.initOBlockManager();
        JUnitUtil.initLogixManager();
        JUnitUtil.initConditionalManager();
        JUnitUtil.initWarrantManager();

        _OBlockMgr = InstanceManager.getDefault(OBlockManager.class);
        _sensorMgr = InstanceManager.getDefault(SensorManager.class);
        _warrantMgr = InstanceManager.getDefault(WarrantManager.class);
    }

    @AfterEach
    public void tearDown() {
        _warrantMgr.dispose();
        _warrantMgr = null;
        _OBlockMgr.dispose();
        _OBlockMgr = null;
        _sensorMgr.dispose();
        _sensorMgr = null;

        if (InstanceManager.containsDefault(ShutDownManager.class)) {
            List<ShutDownTask> list = new ArrayList<>();
            ShutDownManager sm = InstanceManager.getDefault(jmri.ShutDownManager.class);
            for (Runnable r : sm.getRunnables()) {
                if (r instanceof jmri.jmrit.logix.WarrantShutdownTask) {
                    list.add((ShutDownTask)r);
                }
            }
            for ( ShutDownTask t : list) {
                sm.deregister(t);
            }
        }
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.deregisterEditorManagerShutdownTask();
        InstanceManager.getDefault(WarrantManager.class).dispose();
        JUnitUtil.resetWindows(false,false);
        JUnitUtil.tearDown();
    }
}
