package jmri.jmrit.logix;

import java.io.File;
import java.util.List;
import java.util.ArrayList;
import jmri.*;
import jmri.jmrit.display.controlPanelEditor.ControlPanelEditor;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.netbeans.jemmy.operators.JFrameOperator;

import static org.assertj.core.api.Assertions.assertThat;
// import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for running multiple Warrants
 *
 * @author  Pete Cressman 2015
 */
@Timeout(60)
@DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
@DisabledIfSystemProperty(named ="jmri.skipLinkedWarrantTest", matches ="true")
public class LinkedWarrantTest {

    private OBlockManager _OBlockMgr;
    private SensorManager _sensorMgr;
    private WarrantManager _warrantMgr;

    // tests a warrant launching itself. (origin, destination the same to make continuous loop)
//    @Disabled("This test fails on CI")
    @Test
    public void testLoopedWarrant() throws Exception {
        // load and display
        File f = new File("java/test/jmri/jmrit/logix/valid/ShortBlocksTest.xml");
        InstanceManager.getDefault(ConfigureManager.class).load(f);
        JUnitAppender.suppressErrorMessage("Portal elem = null");

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

        JUnitUtil.waitFor(() -> {
            String m =  warrant.getRunningMessage();
            return m.endsWith("Cmd #8.");
        }, "Loopy 1 starts to move at 8th command");

        // Run the train, then checks end location
        assertThat(NXFrameTest.runtimes(route, _OBlockMgr).getDisplayName()).withFailMessage("LoopDeLoop after first leg").isEqualTo(block.getSensor().getDisplayName());
        // It takes 600+ milliseconds per block to execute NXFrameTest.runtimes()
        // i.e. wait at least 600 * route.length for return

        JUnitUtil.waitFor(() -> {
            String m = tableFrame.getStatus();
            return m.equals(Bundle.getMessage("warrantStart", warrant.getTrainName(), warrant.getDisplayName(), block.getDisplayName()));
        }, "LoopDeLoop finished first leg");

        JUnitUtil.waitFor(() -> {
            String m =  warrant.getRunningMessage();
            return m.endsWith("Cmd #8.");
        }, "Loopy 2 starts to move at 8th command");

        assertThat(NXFrameTest.runtimes(route, _OBlockMgr).getDisplayName()).withFailMessage("LoopDeLoop after second leg").isEqualTo(block.getSensor().getDisplayName());

        String linkMsg = Bundle.getMessage("warrantComplete", warrant.getTrainName(), warrant.getDisplayName(), block.getDisplayName());
        JUnitUtil.waitFor(() -> {
            String m = tableFrame.getStatus();
            return m.equals(linkMsg);
        }, "LoopDeLoop finished second leg");

        JUnitUtil.waitFor(() -> {
            String m =  warrant.getRunningMessage();
            return m.endsWith("Cmd #8.");
        }, "Loopy 3 starts to move at 8th command");

        assertThat(NXFrameTest.runtimes(route, _OBlockMgr).getDisplayName()).withFailMessage("LoopDeLoop after last leg").isEqualTo(block.getSensor().getDisplayName());

        JUnitUtil.waitFor(() -> {
            String m = tableFrame.getStatus();
            return m.equals(Bundle.getMessage("warrantComplete", warrant.getTrainName(), warrant.getDisplayName(), block.getDisplayName()));
        }, "LoopDeLoop finished third leg");

        JFrameOperator jfo = new JFrameOperator(tableFrame);
        jfo.requestClose();
        // we may want to use jemmy to close the panel as well.
        assert panel != null;
        ThreadingUtil.runOnGUI( () -> {
            panel.dispose();    // disposing this way allows test to be rerun (i.e. reload panel file) multiple times
        });
    }

    // Tests warrant launching a different warrant with different address. Origin location cannot be destination of the other)
    @Test
    public void testLinkedWarrant() throws Exception {
        // load and display
        File f = new File("java/test/jmri/jmrit/logix/valid/ShortBlocksTest.xml");
        InstanceManager.getDefault(ConfigureManager.class).load(f);
        JUnitAppender.suppressErrorMessage("Portal elem = null");

        final Sensor sensor12 = _sensorMgr.getBySystemName("IS12");
        assertThat(sensor12).withFailMessage("Sensor IS12 not found").isNotNull();

        Sensor sensor1 = _sensorMgr.getBySystemName("IS1");
        assertThat(sensor1).withFailMessage("Senor IS1 not found").isNotNull();
        NXFrameTest.setAndConfirmSensorAction(sensor12, Sensor.ACTIVE, _OBlockMgr.getBySystemName("OB12"));

        final Warrant warrant = _warrantMgr.getWarrant("Loop&Fred");
        assertThat(warrant).withFailMessage("warrant").isNotNull();

        ThreadingUtil.runOnGUI(() -> {
            WarrantTableFrame tableFrame = WarrantTableFrame.getDefault();
            // WarrantTable.runTrain() returns a string that is not null if the
            // warrant can't be started
            assertThat(tableFrame.runTrain(warrant, Warrant.MODE_RUN)).withFailMessage("Warrant starts").isNull(); // start run
        });

        JFrameOperator jfo = new JFrameOperator(Bundle.getMessage("WarrantTable"));
        Assertions.assertNotNull(jfo,"WarrantTable not Found");

        JFrameOperator jfoPanel = new JFrameOperator("LinkedWarrantsTest");
        Assertions.assertNotNull(jfoPanel,"LinkedWarrantsTest panel not Found");

        JUnitUtil.waitFor(() -> {
            String m =  warrant.getRunningMessage();
            return m.endsWith("Cmd #8.");
        }, "Train starts to move at 8th command, but running message was " + warrant.getRunningMessage());

       // OBlock of route
        String[] route1 = {"OB12", "OB1", "OB3", "OB5", "OB6", "OB7", "OB9", "OB11", "OB12"};
        OBlock block = _OBlockMgr.getOBlock("OB12");

        // Run the train, then checks end location
        assertThat(NXFrameTest.runtimes(route1, _OBlockMgr)).withFailMessage("Train after first leg").isEqualTo(block.getSensor());
        // It takes 500+ milliseconds per block to execute NXFrameTest.runtimes()

        // "Loop&Fred" links to "WestToEast". Get start for "WestToEast" occupied quickly
        NXFrameTest.setAndConfirmSensorAction(sensor1, Sensor.ACTIVE, _OBlockMgr.getBySystemName("OB1"));

        Warrant ww = _warrantMgr.getWarrant("WestToEast");
        Assertions.assertNotNull(ww,"warrant WestToEast exists");

        JUnitUtil.waitFor(() -> {
            String m =  ww.getRunningMessage();
            return m.endsWith("Cmd #9.");
        }, "Train Fred starts to move at 8th command");

        String[] route2 = {"OB1", "OB3", "OB5", "OB6", "OB7", "OB9", "OB11"};
        block = _OBlockMgr.getOBlock("OB11");

        assertThat(NXFrameTest.runtimes(route2, _OBlockMgr)).withFailMessage("Train after second leg").isEqualTo(block.getSensor());

        // passed test - cleanup.  Do it here so failure leaves traces.
        jfo.requestClose();
        jfoPanel.requestClose();
        jfo.waitClosed();
        jfoPanel.waitClosed();

    }

    // tests a warrant running a train out and launching a return train
    // Both warrants have the same address and origin of each is destination of the other
//    @Disabled("This test fails on CI")
    @Test
    public void testBackAndForth() throws Exception {
        // load and display
        File f = new File("java/test/jmri/jmrit/logix/valid/ShortBlocksTest.xml");
        InstanceManager.getDefault(ConfigureManager.class).load(f);
        JUnitAppender.suppressErrorMessage("Portal elem = null");

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
        Sensor outEndSensor = _OBlockMgr.getOBlock("OB11").getSensor();
        String[] routeBack = {"OB11", "OB9", "OB7", "OB6", "OB5", "OB3", "OB1"};
        Sensor backEndSensor = _OBlockMgr.getOBlock("OB1").getSensor();

        // WarrantTable.runTrain() returns a string that is not null if the
        // warrant can't be started
        assertThat(tableFrame.runTrain(outWarrant, Warrant.MODE_RUN)).withFailMessage("Warrant starts").isNull(); // start run

        JUnitUtil.waitFor(() -> {
            String m =  outWarrant.getRunningMessage();
            return m.endsWith("Cmd #8.");
        }, "WestToEastLink starts to move at 8th command");

        // Run the train, then checks end location
        assertThat(NXFrameTest.runtimes(routeOut, _OBlockMgr)).withFailMessage("Train after first leg").isEqualTo(outEndSensor);
        // It takes 500+ milliseconds per block to execute NXFrameTest.runtimes()
        // i.e. wait at least 600 * (route.length - 1) for return

        String outBlockName = _OBlockMgr.getOBlock("OB11").getDisplayName();
        JUnitUtil.waitFor(() -> {
            String m = tableFrame.getStatus();
            return m.equals(Bundle.getMessage("warrantComplete",
                                outWarrant.getTrainName(), outWarrant.getDisplayName(),
                                outBlockName));
        }, "WestToEastLink finished first leg out");

        JUnitUtil.waitFor(() -> {
            String m =  backWarrant.getRunningMessage();
            return m.endsWith("Cmd #8.");
        }, "EastToWestLink starts to move at 8th command");

        assertThat(NXFrameTest.runtimes(routeBack, _OBlockMgr)).withFailMessage("Train after second leg").isEqualTo(backEndSensor);
        // It takes 500+ milliseconds per block to execute NXFrameTest.runtimes()

        JUnitUtil.waitFor(() -> {
            String m = tableFrame.getStatus();
            return m.equals(Bundle.getMessage("warrantComplete",
                    backWarrant.getTrainName(), backWarrant.getDisplayName(),
                    _OBlockMgr.getOBlock("OB1").getDisplayName()));
        }, "EastToWestLink finished second leg back");

        JUnitUtil.waitFor(() -> {
            String m =  outWarrant.getRunningMessage();
            return m.endsWith("Cmd #8.");
        }, "WestToEastLink starts to move at 8th command");

        assertThat(NXFrameTest.runtimes(routeOut, _OBlockMgr)).withFailMessage("Train after third leg").isEqualTo(outEndSensor);
        // It takes 500+ milliseconds per block to execute NXFrameTest.runtimes()

        JUnitUtil.waitFor(() -> {
            String m = tableFrame.getStatus();
            return m.equals(Bundle.getMessage("warrantComplete",
                    outWarrant.getTrainName(), outWarrant.getDisplayName(),
                    outBlockName));
        }, "WestToEastLink finished third leg");

        JUnitUtil.waitFor(() -> {
            String m =  backWarrant.getRunningMessage();
            return m.endsWith("Cmd #8.") || m.endsWith("Cmd #9.");
        }, "EastToWestLink starts to move at 8th command");

        assertThat(NXFrameTest.runtimes(routeBack, _OBlockMgr)).withFailMessage("Train after fourth leg").isEqualTo(backEndSensor);
        // It takes 600+ milliseconds per block to execute NXFrameTest.runtimes()

        outWarrant.stopWarrant(true, false);
        backWarrant.stopWarrant(true, false);
            // passed test - cleanup.  Do it here so failure leaves traces.
            JFrameOperator jfo = new JFrameOperator(tableFrame);
            jfo.requestClose();
            // we may want to use jemmy to close the panel as well.
        assert panel != null;
        ThreadingUtil.runOnGUI( () -> {
            panel.dispose();    // disposing this way allows test to be rerun (i.e. reload panel file) multiple times
        });
    }

    // Tests warrant launching 3 different warrants mid script - tinker to Evers to Chance (1910 Chicago Cubs)
    @Test
    public void testLinkedMidScript() throws Exception {
        // load and display
        File f = new File("java/test/jmri/jmrit/logix/valid/NXWarrantTest.xml");
        InstanceManager.getDefault(ConfigureManager.class).load(f);
        JUnitAppender.suppressErrorMessage("Portal elem = null");

        // Tinker start block
        Sensor sensor0 = _sensorMgr.getBySystemName("IS0");
        assertThat(sensor0).withFailMessage("Senor IS0 not found").isNotNull();
        // wait block OB0 occupied
        NXFrameTest.setAndConfirmSensorAction(sensor0, Sensor.ACTIVE, _OBlockMgr.getBySystemName("OB0"));

        // Evers start block
        Sensor sensor7 = _sensorMgr.getBySystemName("IS7");
        assertThat(sensor7).withFailMessage("Senor IS7 not found").isNotNull();
        // wait block OB7 occupied
        NXFrameTest.setAndConfirmSensorAction(sensor7, Sensor.ACTIVE, _OBlockMgr.getBySystemName("OB7"));

        // Chance start block
        Sensor sensor6 = _sensorMgr.getBySystemName("IS6");
        assertThat(sensor6).withFailMessage("Senor IS6 not found").isNotNull();
        NXFrameTest.setAndConfirmSensorAction(sensor6, Sensor.ACTIVE, _OBlockMgr.getBySystemName("OB6"));

        Warrant w = _warrantMgr.getWarrant("Tinker");
        assertThat(w).withFailMessage("warrant").isNotNull();

        ThreadingUtil.runOnGUI(() -> {
            WarrantTableFrame tableFrame = WarrantTableFrame.getDefault();
            // WarrantTable.runTrain() returns a string that is not null if the
            // warrant can't be started
            assertThat(tableFrame.runTrain(w, Warrant.MODE_RUN)).withFailMessage("Warrant starts").isNull(); // start run
        });
        JFrameOperator jfo = new JFrameOperator(Bundle.getMessage("WarrantTable"));
        Assertions.assertNotNull(jfo,"WarrantTable not Found");

        JFrameOperator jfoPanel = new JFrameOperator("NXWarrantTest");
        Assertions.assertNotNull(jfoPanel,"NXWarrantTest panel not Found");

        JUnitUtil.waitFor(() -> {
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

        JUnitUtil.waitFor(() -> {
            String m =  ww.getRunningMessage();
            return m.endsWith("Cmd #8.");
        }, "Evers starts to move at 8th command");

        String[] route2 = {"OB7", "OB3", "OB2", "OB1"};
        block = _OBlockMgr.getOBlock("OB1");

        assertThat(NXFrameTest.runtimes(route2, _OBlockMgr).getDisplayName()).withFailMessage("Evers after second leg").isEqualTo(block.getSensor().getDisplayName());
        // It takes 600+ milliseconds per block to execute NXFrameTest.runtimes()

        Warrant www = _warrantMgr.getWarrant("Chance");

        JUnitUtil.waitFor(() -> {
            String m =  www.getRunningMessage();
            return m.endsWith("Cmd #8.") || m.endsWith("Cmd #9.") || m.endsWith("Cmd #10."); // in case runs fast
        }, "Chance starts to move at 8th command");

        String[] route3 = {"OB6", "OB3", "OB4", "OB5"};
        block = _OBlockMgr.getOBlock("OB5");

        assertThat(NXFrameTest.runtimes(route3, _OBlockMgr).getDisplayName()).withFailMessage("Chance after third leg").isEqualTo(block.getSensor().getDisplayName());
        // It takes 600+ milliseconds per block to execute NXFrameTest.runtimes()

        // passed test - cleanup.  Do it here so failure leaves traces.
        
        jfo.requestClose();
        jfoPanel.requestClose();

        jfo.waitClosed();
        jfoPanel.waitClosed();

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

        WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);

        _OBlockMgr = InstanceManager.getDefault(OBlockManager.class);
        _sensorMgr = InstanceManager.getDefault(SensorManager.class);
        _warrantMgr = InstanceManager.getDefault(WarrantManager.class);
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.removeMatchingThreads("Engineer(");

        _warrantMgr.dispose();
        _warrantMgr = null;
        InstanceManager.getDefault(WarrantManager.class).dispose();
        _OBlockMgr.dispose();
        _OBlockMgr = null;
        _sensorMgr.dispose();
        _sensorMgr = null;

        JUnitUtil.deregisterBlockManagerShutdownTask();
        if (InstanceManager.containsDefault(ShutDownManager.class)) {
            List<ShutDownTask> list = new ArrayList<>();
            ShutDownManager sm = InstanceManager.getDefault(ShutDownManager.class);
            for (Runnable r : sm.getRunnables()) {
                if (r instanceof jmri.jmrit.logix.WarrantShutdownTask) {
                    list.add((ShutDownTask)r);
                }
            }
            for ( ShutDownTask t : list) {
                sm.deregister(t);
            }
        }

        JUnitUtil.resetWindows(false,false);
        JUnitUtil.tearDown();
    }
}
