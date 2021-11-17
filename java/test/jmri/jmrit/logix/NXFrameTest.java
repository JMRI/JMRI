package jmri.jmrit.logix;

import java.awt.GraphicsEnvironment;
import java.io.File;
import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.jmrit.display.controlPanelEditor.ControlPanelEditor;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * Tests for the NXFrame class, and its interactions with Warrants.
 *
 * @author Pete Cressman 2015
 *
 * TODO - test error conditions
 */
@Timeout(30)
public class NXFrameTest {

    OBlockManager _OBlockMgr;
    SensorManager _sensorMgr;

    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testGetDefault() {
        NXFrame nxFrame = new NXFrame();
        assertThat(nxFrame).withFailMessage("NXFrame").isNotNull();
        JUnitUtil.dispose(nxFrame);
    }

    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    @DisabledIfSystemProperty(named ="jmri.skipTestsRequiringSeparateRunning", matches ="true")
    public void testRoutePanel() throws Exception {
        NXFrame nxFrame = new NXFrame();
        assertThat(nxFrame).withFailMessage("NXFrame").isNotNull();

        JFrameOperator jfo = new JFrameOperator(nxFrame);

        nxFrame.setVisible(true);
        JemmyUtil.pressButton(jfo, Bundle.getMessage("Calculate"));

        JemmyUtil.confirmJOptionPane(jfo, Bundle.getMessage("WarningTitle"), Bundle.getMessage("SetEndPoint", Bundle.getMessage("OriginBlock")), "OK");

        nxFrame._origin.blockBox.setText("NowhereBlock");

        JemmyUtil.pressButton(jfo, Bundle.getMessage("Calculate"));

        JemmyUtil.confirmJOptionPane(jfo, Bundle.getMessage("WarningTitle"), Bundle.getMessage("BlockNotFound", "NowhereBlock"), "OK");

        JemmyUtil.confirmJOptionPane(jfo, Bundle.getMessage("WarningTitle"), Bundle.getMessage("SetEndPoint", Bundle.getMessage("OriginBlock")), "OK");

        jfo.requestClose();
    }

    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    @DisabledIfSystemProperty(named ="jmri.skipTestsRequiringSeparateRunning", matches ="true")
    public void testNXWarrantSetup() throws Exception {
        // load and display
        File f = new File("java/test/jmri/jmrit/logix/valid/NXWarrantTest.xml");
        InstanceManager.getDefault(ConfigureManager.class).load(f);
        jmri.util.JUnitAppender.suppressErrorMessage("Portal elem = null");

        WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);

        _OBlockMgr = InstanceManager.getDefault(OBlockManager.class);
        _sensorMgr = InstanceManager.getDefault(SensorManager.class);

        NXFrame nxFrame = new NXFrame();
        nxFrame.setVisible(true);

        JFrameOperator nfo = new JFrameOperator(nxFrame);

        nxFrame.mouseClickedOnBlock(_OBlockMgr.getBySystemName("OB0"));
        nxFrame._origin.portalBox.setSelectedItem("Enter");
        nxFrame.mouseClickedOnBlock(_OBlockMgr.getBySystemName("OB10"));
        nxFrame._destination.portalBox.setSelectedItem("Exit");

        JemmyUtil.pressButton(nfo, Bundle.getMessage("Calculate"));

        JDialogOperator jdo = new JDialogOperator(nfo,Bundle.getMessage("DialogTitle"));

        JemmyUtil.pressButton(jdo, Bundle.getMessage("ButtonReview"));

        JemmyUtil.confirmJOptionPane(nfo, Bundle.getMessage("WarningTitle"), Bundle.getMessage("SelectRoute"), "OK");

        // click the third radio button in the dialog
        JRadioButtonOperator jrbo = new JRadioButtonOperator(jdo,3);
        jrbo.clickMouse();
        // then the Review Button
        JemmyUtil.pressButton(jdo, Bundle.getMessage("ButtonReview"));

        // click the 1st radio button in the dialog
        jrbo = new JRadioButtonOperator(jdo,1);
        jrbo.clickMouse();
        // then the Review Button
        JemmyUtil.pressButton(jdo, Bundle.getMessage("ButtonReview"));

        nxFrame._speedUtil.setRampThrottleIncrement(0.05f);

        JemmyUtil.pressButton(jdo, Bundle.getMessage("ButtonSelect"));
        nxFrame.setMaxSpeed(2);
        JemmyUtil.pressButton(nfo, Bundle.getMessage("ButtonRunNX"));
        JemmyUtil.confirmJOptionPane(nfo, Bundle.getMessage("WarningTitle"), Bundle.getMessage("badSpeed", "2"), "OK");

        nxFrame.setMaxSpeed(0.6f);
        JemmyUtil.pressButton(nfo, Bundle.getMessage("ButtonRunNX"));
        JemmyUtil.confirmJOptionPane(nfo, Bundle.getMessage("WarningTitle"), Bundle.getMessage("BadDccAddress", ""), "OK");

        nxFrame._speedUtil.setAddress("666");
        nxFrame.setTrainInfo("Nick");
        JemmyUtil.pressButton(nfo, Bundle.getMessage("ButtonRunNX"));

        nfo.requestClose();
        // we may want to use jemmy to close the panel as well.
        ControlPanelEditor panel = (ControlPanelEditor) jmri.util.JmriJFrame.getFrame("NXWarrantTest");
        panel.dispose();    // disposing this way allows test to be rerun (i.e. reload panel file) multiple times
    }

    @Test
    @Timeout(60)
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    @DisabledIfSystemProperty(named ="jmri.skipTestsRequiringSeparateRunning", matches ="true")
    public void testNXWarrant() throws Exception {
        // The first part of this test duplicates testNXWarrantSetup().  It
        // then goes on to test a Warrant through the WarrantTableFrame.
        // it is the WarrantTableframe portion of this test that hangs.

        // load and display
        File f = new File("java/test/jmri/jmrit/logix/valid/NXWarrantTest.xml");
        InstanceManager.getDefault(ConfigureManager.class).load(f);
        jmri.util.JUnitAppender.suppressErrorMessage("Portal elem = null");

        WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);

        _OBlockMgr = InstanceManager.getDefault(OBlockManager.class);
        _sensorMgr = InstanceManager.getDefault(SensorManager.class);
        OBlock block = _OBlockMgr.getBySystemName("OB0");

        NXFrame nxFrame = new NXFrame();
        nxFrame.setVisible(true);

        JFrameOperator nfo = new JFrameOperator(nxFrame);

        nxFrame.mouseClickedOnBlock(block);
        nxFrame._origin.portalBox.setSelectedItem("Enter");
        nxFrame.mouseClickedOnBlock(_OBlockMgr.getBySystemName("OB10"));
        nxFrame._destination.portalBox.setSelectedItem("Exit");

        JemmyUtil.pressButton(nfo, Bundle.getMessage("Calculate"));

        JDialogOperator jdo = new JDialogOperator(nfo,Bundle.getMessage("DialogTitle"));

        JemmyUtil.pressButton(jdo, Bundle.getMessage("ButtonReview"));

        JemmyUtil.confirmJOptionPane(nfo, Bundle.getMessage("WarningTitle"), Bundle.getMessage("SelectRoute"), "OK");

        // click the third radio button in the dialog
        JRadioButtonOperator jrbo = new JRadioButtonOperator(jdo,3);
        jrbo.clickMouse();
        // then the Review Button
        JemmyUtil.pressButton(jdo, Bundle.getMessage("ButtonReview"));

        // click the 1st radio button in the dialog
        jrbo = new JRadioButtonOperator(jdo,1);
        jrbo.clickMouse();
        // then the Review Button
        JemmyUtil.pressButton(jdo, Bundle.getMessage("ButtonReview"));
        JemmyUtil.pressButton(jdo, Bundle.getMessage("ButtonSelect"));

        nxFrame._speedUtil.setRampThrottleIncrement(0.05f);
        nxFrame._speedUtil.setRampTimeIncrement(100);
        nxFrame.setMaxSpeed(0.6f);
        nxFrame._speedUtil.setAddress("666");
        nxFrame.setTrainInfo("Nick");
        JemmyUtil.pressButton(nfo, Bundle.getMessage("ButtonRunNX"));

        WarrantTableFrame tableFrame = WarrantTableFrame.getDefault();
        assertThat(tableFrame).withFailMessage("tableFrame").isNotNull();

        WarrantTableModel model = tableFrame.getModel();
        assertThat(model).withFailMessage("tableFrame model").isNotNull();

        JUnitUtil.waitFor(() -> {
            return model.getRowCount() > 1;
        }, "NXWarrant loaded into table");

        Warrant warrant = tableFrame.getModel().getWarrantAt(model.getRowCount()-1);

        assertThat(warrant).withFailMessage("warrant").isNotNull();
        assertThat(warrant.getBlockOrders()).withFailMessage("warrant.getBlockOrders(").isNotNull();
        warrant.getBlockOrders();
        assertThat(warrant.getBlockOrders().size()).withFailMessage("Num Blocks in Route").isEqualTo(7);
        assertThat(warrant.getThrottleCommands().size()>5).withFailMessage("Num Comands").isTrue();

        String name = block.getDisplayName();
        jmri.util.JUnitUtil.waitFor(
            ()->{return warrant.getRunningMessage().equals(Bundle.getMessage("waitForDelayStart", warrant.getTrainName(), name));},
            "Waiting message");
        Sensor sensor0 = _sensorMgr.getBySystemName("IS0");
        assertThat(sensor0).withFailMessage("Senor IS0 not found").isNotNull();

        NXFrameTest.setAndConfirmSensorAction(sensor0, Sensor.ACTIVE, block);

        final OBlock testblock = block;
        JUnitUtil.waitFor(() -> {
            return ((testblock.getState() & (OBlock.ALLOCATED | OBlock.OCCUPIED)) != 0);
        }, "Start Block Active");

        JUnitUtil.waitFor(() -> {
            return Bundle.getMessage("atHalt", name).equals(warrant.getRunningMessage());
        }, "Warrant processed sensor change");

        assertThat(Bundle.getMessage("atHalt", block.getDisplayName())).withFailMessage("Halted/Resume message").isEqualTo(warrant.getRunningMessage());

        jmri.util.ThreadingUtil.runOnGUI(() -> {
            warrant.controlRunTrain(Warrant.RESUME);
        });

        jmri.util.JUnitUtil.waitFor(() -> {
            String m =  warrant.getRunningMessage();
            return m.endsWith("Cmd #8.");
        }, "Train starts to move at 8th command");

        // OBlock sensor names
        String[] route = {"OB0", "OB1", "OB2", "OB3", "OB7", "OB5", "OB10"};
        block = _OBlockMgr.getOBlock("OB10");
        // runtimes() in next line runs the train, then checks location
        assertThat(runtimes(route, _OBlockMgr).getDisplayName()).withFailMessage("Train in last block").isEqualTo(block.getSensor().getDisplayName());

        jmri.util.ThreadingUtil.runOnGUI(() -> {
            warrant.controlRunTrain(Warrant.ABORT);
        });

        // passed test - cleanup.  Do it here so failure leaves traces.
        JFrameOperator jfo = new JFrameOperator(tableFrame);
        jfo.requestClose();
        // we may want to use jemmy to close the panel as well.
        ControlPanelEditor panel = (ControlPanelEditor) jmri.util.JmriJFrame.getFrame("NXWarrantTest");
        panel.dispose();    // disposing this way allows test to be rerun (i.e. reload panel file) multiple times
    }

    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    @DisabledIfSystemProperty(named ="jmri.skipTestsRequiringSeparateRunning", matches ="true")
    public void testWarrantLoopRun() throws Exception {
        // load and display
        File f = new File("java/test/jmri/jmrit/logix/valid/NXWarrantTest.xml");
        InstanceManager.getDefault(ConfigureManager.class).load(f);
        jmri.util.JUnitAppender.suppressErrorMessage("Portal elem = null");

        WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);

        _OBlockMgr = InstanceManager.getDefault(OBlockManager.class);
        _sensorMgr = InstanceManager.getDefault(SensorManager.class);

        Sensor sensor3 = _sensorMgr.getBySystemName("IS3");
        assertThat(sensor3).withFailMessage("Senor IS3 not found").isNotNull();

        NXFrameTest.setAndConfirmSensorAction(sensor3, Sensor.ACTIVE, _OBlockMgr.getBySystemName("OB3"));

        WarrantTableFrame tableFrame = WarrantTableFrame.getDefault();
        assertThat(tableFrame).withFailMessage("tableFrame").isNotNull();

        Warrant warrant = tableFrame.getModel().getWarrantAt(0);
        assertThat(warrant).withFailMessage("warrant").isNotNull();

        tableFrame.runTrain(warrant, Warrant.MODE_RUN);
        jmri.util.JUnitUtil.waitFor(() -> {
            String m =  warrant.getRunningMessage();
            return m.endsWith("Cmd #3.");
        }, "Train is moving at 3rd command");

       // OBlock sensor names
        String[] route = {"OB3", "OB4", "OB5", "OB10", "OB0", "OB1", "OB2", "OB3"};
        OBlock block = _OBlockMgr.getOBlock("OB3");
        // runtimes() in next line runs the train, then checks location
        assertThat(runtimes(route, _OBlockMgr).getDisplayName()).withFailMessage("Train in last block").isEqualTo(block.getSensor().getDisplayName());

        // passed test - cleanup.  Do it here so failure leaves traces.
        JFrameOperator jfo = new JFrameOperator(tableFrame);
        jfo.requestClose();
        // we may want to use jemmy to close the panel as well.
        ControlPanelEditor panel = (ControlPanelEditor) jmri.util.JmriJFrame.getFrame("NXWarrantTest");
        panel.dispose();    // disposing this way allows test to be rerun (i.e. reload panel file) multiple times
    }

    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    @DisabledIfSystemProperty(named ="jmri.skipTestsRequiringSeparateRunning", matches ="true")
    public void testWarrantRampHalt() throws Exception {
        // load and display
        File f = new File("java/test/jmri/jmrit/logix/valid/NXWarrantTest.xml");
        InstanceManager.getDefault(ConfigureManager.class).load(f);
        jmri.util.JUnitAppender.suppressErrorMessage("Portal elem = null");

        WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);

        _OBlockMgr = InstanceManager.getDefault(OBlockManager.class);
        _sensorMgr = InstanceManager.getDefault(SensorManager.class);

        Sensor sensor1 = _sensorMgr.getBySystemName("IS1");
        assertThat(sensor1).withFailMessage("Senor IS1 not found").isNotNull();

        NXFrameTest.setAndConfirmSensorAction(sensor1, Sensor.ACTIVE, _OBlockMgr.getBySystemName("OB1"));

        WarrantTableFrame tableFrame = WarrantTableFrame.getDefault();
        assertThat(tableFrame).withFailMessage("tableFrame").isNotNull();

        Warrant warrant = tableFrame.getModel().getWarrantAt(1);
        assertThat(warrant).withFailMessage("warrant").isNotNull();

        tableFrame.runTrain(warrant, Warrant.MODE_RUN);

        SpeedUtil sp = warrant.getSpeedUtil();
        sp.setRampThrottleIncrement(0.15f);
        sp.setRampTimeIncrement(100);

        jmri.util.JUnitUtil.waitFor(() -> {
            String m =  warrant.getRunningMessage();
            return m.endsWith("Cmd #8.");
        }, "Train starts to move at 8th command");

       // OBlock sensor names
        String[] route1 = {"OB1", "OB6", "OB3"};
        OBlock block = _OBlockMgr.getOBlock("OB3");
        // runtimes() in next line runs the train, then checks location
        assertThat(runtimes(route1,_OBlockMgr).getDisplayName()).withFailMessage("Train in block OB3").isEqualTo(block.getSensor().getDisplayName());

        warrant.controlRunTrain(Warrant.RAMP_HALT); // user interrupts script
        JUnitUtil.waitFor(100);     // waitEmpty(10) causes a lot of failures on Travis GUI
        jmri.util.JUnitUtil.waitFor(() -> {
            String m =  warrant.getRunningMessage();
            return (m.startsWith("Halted in block"));
        }, "Train Halted");

        warrant.controlRunTrain(Warrant.RESUME);
        JUnitUtil.waitFor(100);     // waitEmpty(10) causes a lot of failures on Travis GUI


        jmri.util.JUnitUtil.waitFor(() -> {
            String m =  warrant.getRunningMessage();
            return m.startsWith("Running in Block OB3") ||
                    m.startsWith("Overdue for arrival at block");
        }, "Train Resumed");

        String[] route2 = {"OB3", "OB7", "OB5"};
        block = _OBlockMgr.getOBlock("OB5");
        // runtimes() in next line runs the train, then checks location
        assertThat(runtimes(route2, _OBlockMgr).getDisplayName()).withFailMessage("Train in last block").isEqualTo(block.getSensor().getDisplayName());

        // passed test - cleanup.  Do it here so failure leaves traces.
        JFrameOperator jfo = new JFrameOperator(tableFrame);
        jfo.requestClose();
        // we may want to use jemmy to close the panel as well.
        ControlPanelEditor panel = (ControlPanelEditor) jmri.util.JmriJFrame.getFrame("NXWarrantTest");
        panel.dispose();    // disposing this way allows test to be rerun (i.e. reload panel file) multiple times
    }

    /**
     * Simulates the movement of a warranted train over its route.
     * <p>Works through a list of OBlocks, gets its sensor,
     * activates it, then inactivates the previous OBlock sensor.
     * Leaves last sensor ACTIVE to show the train stopped there.
     * @param route Array of OBlock names of the route
     * @param mgr OBLock manager
     * @return active end sensor
     * @throws Exception exception thrown
     */
    protected static Sensor runtimes(String[] route, OBlockManager mgr) throws Exception {
        Sensor sensor = null;
        for (int i = 1; i < route.length; i++) {
            sensor = moveToNextBlock(i, route, mgr);
        }
        return sensor;
    }

    protected static Sensor moveToNextBlock(int idx, String[] route, OBlockManager mgr) {
        assertThat(idx > 0 && idx < route.length).withFailMessage("Index "+ idx + " invalid ").isTrue();

        OBlock fromBlock = mgr.getOBlock(route[idx - 1]);
        Sensor fromSensor = fromBlock.getSensor();
        assertThat(fromSensor).withFailMessage("fromSensor not found").isNotNull();

        OBlock toBlock = mgr.getOBlock(route[idx]);
        Sensor toSensor = toBlock.getSensor();
        assertThat(toSensor).withFailMessage("toSensor not found").isNotNull();

        JUnitUtil.waitFor(300);
        NXFrameTest.setAndConfirmSensorAction(toSensor, Sensor.ACTIVE, toBlock);

        JUnitUtil.waitFor(200);
        NXFrameTest.setAndConfirmSensorAction(fromSensor, Sensor.INACTIVE, fromBlock);

        return toSensor;
    }


    protected static void setAndConfirmSensorAction(Sensor sensor, int state, OBlock block)  {
        if (state == Sensor.ACTIVE) {
            jmri.util.ThreadingUtil.runOnLayout(() -> {
                Throwable thrown = catchThrowable( () -> sensor.setState(Sensor.ACTIVE));
                assertThat(thrown).withFailMessage("Set "+ sensor.getDisplayName()+" ACTIVE Exception: " + thrown).isNull();
            });
            OBlock b = block;
            jmri.util.JUnitUtil.waitFor(() -> {
                return (b.getState() & OBlock.OCCUPIED) != 0;
            }, b.getDisplayName() + " occupied");
        } else if (state == Sensor.INACTIVE) {
            jmri.util.ThreadingUtil.runOnLayout(() -> {
                Throwable thrown = catchThrowable( () -> sensor.setState(Sensor.INACTIVE));
                assertThat(thrown).withFailMessage("Set "+ sensor.getDisplayName()+" INACTIVE Exception: " + thrown).isNull();
            });
            OBlock b = block;
            jmri.util.JUnitUtil.waitFor(() -> {
                return (b.getState() & OBlock.OCCUPIED) == 0;
            }, b.getDisplayName() + " unoccupied");
        }
    }

    @BeforeEach
    public void setUp() throws Exception {
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
        WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);
        JUnitUtil.initWarrantManager();
    }

    @AfterEach
    public void tearDown() throws Exception {
        JUnitUtil.removeMatchingThreads("Engineer(");
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.deregisterEditorManagerShutdownTask();
        InstanceManager.getDefault(WarrantManager.class).dispose();
        JUnitUtil.resetWindows(false,false);
        JUnitUtil.tearDown();
    }

}
