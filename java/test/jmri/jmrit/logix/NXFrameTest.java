package jmri.jmrit.logix;

import java.io.File;
import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.jmrit.display.controlPanelEditor.ControlPanelEditor;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the NXFrame class, and its interactions with Warrants.
 *
 * @author Pete Cressman 2015
 *
 * TODO - test error conditions
 */
@Timeout(30)
@DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
public class NXFrameTest {

    private OBlockManager _OBlockMgr;
    private SensorManager _sensorMgr;

    @Test
    public void testGetDefault() {
        NXFrame nxFrame = new NXFrame();
        assertNotNull( nxFrame, "NXFrame");
        JUnitUtil.dispose(nxFrame);
    }

    @Test
    @DisabledIfSystemProperty(named ="jmri.skipTestsRequiringSeparateRunning", matches ="true")
    public void testRoutePanel() throws Exception {
        NXFrame nxFrame = new NXFrame();
        assertNotNull( nxFrame, "NXFrame");

        JFrameOperator jfo = new JFrameOperator(nxFrame);

        nxFrame.setVisible(true);
        JemmyUtil.pressButton(jfo, Bundle.getMessage("Calculate"));

        JemmyUtil.confirmJOptionPane(jfo, Bundle.getMessage("WarningTitle"), Bundle.getMessage("SetEndPoint", Bundle.getMessage("OriginBlock")), "OK");

        nxFrame._origin.blockBox.setText("NowhereBlock");

        JemmyUtil.pressButton(jfo, Bundle.getMessage("Calculate"));

        JemmyUtil.confirmJOptionPane(jfo, Bundle.getMessage("WarningTitle"), Bundle.getMessage("BlockNotFound", "NowhereBlock"), "OK");

        JemmyUtil.confirmJOptionPane(jfo, Bundle.getMessage("WarningTitle"), Bundle.getMessage("SetEndPoint", Bundle.getMessage("OriginBlock")), "OK");

        jfo.requestClose();
        jfo.waitClosed();
    }

    @Test
    @DisabledIfSystemProperty(named ="jmri.skipTestsRequiringSeparateRunning", matches ="true")
    public void testNXWarrantSetup() throws Exception {
        // load and display
        File f = new File("java/test/jmri/jmrit/logix/valid/NXWarrantTest.xml");
        InstanceManager.getDefault(ConfigureManager.class).load(f);
        JUnitAppender.suppressErrorMessage("Portal elem = null");

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
        // disposing this way allows test to be rerun (i.e. reload panel file) multiple times
        Boolean retVal = jmri.util.ThreadingUtil.runOnGUIwithReturn(() -> {
            panel.dispose();
            return true;
        });
        assertTrue(retVal);
    }

    @Test
    @Timeout(60)
    @DisabledIfSystemProperty(named ="jmri.skipTestsRequiringSeparateRunning", matches ="true")
    public void testNXWarrant() throws Exception {
        // The first part of this test duplicates testNXWarrantSetup().  It
        // then goes on to test a Warrant through the WarrantTableFrame.
        // it is the WarrantTableframe portion of this test that hangs.

        // load and display
        File f = new File("java/test/jmri/jmrit/logix/valid/NXWarrantTest.xml");
        InstanceManager.getDefault(ConfigureManager.class).load(f);
        JUnitAppender.suppressErrorMessage("Portal elem = null");

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
        assertNotNull( tableFrame, "tableFrame");

        WarrantTableModel model = tableFrame.getModel();
        assertNotNull( model, "tableFrame model");

        JUnitUtil.waitFor(() -> model.getRowCount() > 1, "NXWarrant loaded into table");

        Warrant warrant = tableFrame.getModel().getWarrantAt(model.getRowCount()-1);

        assertNotNull( warrant, "warrant");
        assertNotNull( warrant.getBlockOrders(), "warrant.getBlockOrders()");
        assertEquals( 7, warrant.getBlockOrders().size(),"7 Blocks in Route");
        assertTrue( warrant.getThrottleCommands().size()>5 , "Num Comands");

        assertNotNull(block);
        String name = block.getDisplayName();
        JUnitUtil.waitFor(()->{
            return warrant.getRunningMessage().equals(Bundle.getMessage("waitForDelayStart", warrant.getTrainName(), name));},
            "Waiting message");
        Sensor sensor0 = _sensorMgr.getBySystemName("IS0");
        assertNotNull( sensor0, "Senor IS0 not found");

        NXFrameTest.setAndConfirmSensorAction(sensor0, Sensor.ACTIVE, block);

        final OBlock testblock = block;
        JUnitUtil.waitFor(() -> {
            return ((testblock.getState() & (OBlock.ALLOCATED | OBlock.OCCUPIED)) != 0);
        }, "Start Block Active");

        JUnitUtil.waitFor(() -> {
            JUnitUtil.waitFor(200);
            return Bundle.getMessage("RampHalt", warrant.getTrainName(), testblock.getDisplayName()).equals(warrant.getRunningMessage());
        }, "Warrant processed sensor change");

        ThreadingUtil.runOnGUI(() -> {
            warrant.controlRunTrain(Warrant.RESUME);
        });

        JUnitUtil.waitFor(() -> {
            String m =  warrant.getRunningMessage();
            return m.endsWith("Cmd #8.");
        }, "Train starts to move at 8th command");

        // OBlock sensor names
        String[] route = {"OB0", "OB1", "OB2", "OB3", "OB7", "OB5", "OB10"};
        block = _OBlockMgr.getOBlock("OB10");
        // runtimes() in next line runs the train, then checks location
        assertEquals( block.getSensor().getDisplayName(),
            runtimes(route, _OBlockMgr).getDisplayName(), "Train in last block");

        ThreadingUtil.runOnGUI(() -> {
            warrant.controlRunTrain(Warrant.ABORT);
        });

        // passed test - cleanup.  Do it here so failure leaves traces.
        JFrameOperator jfo = new JFrameOperator(tableFrame);
        jfo.requestClose();
        jfo.waitClosed();
        // we may want to use jemmy to close the panel as well.
        ControlPanelEditor panel = (ControlPanelEditor) jmri.util.JmriJFrame.getFrame("NXWarrantTest");
        // disposing this way allows test to be rerun (i.e. reload panel file) multiple times
        Boolean retVal = jmri.util.ThreadingUtil.runOnGUIwithReturn(() -> {
            panel.dispose();
            return true;
        });
        assertTrue(retVal);
    }

    @Test
    @DisabledIfSystemProperty(named ="jmri.skipTestsRequiringSeparateRunning", matches ="true")
    public void testWarrantLoopRun() throws Exception {
        // load and display
        File f = new File("java/test/jmri/jmrit/logix/valid/NXWarrantTest.xml");
        InstanceManager.getDefault(ConfigureManager.class).load(f);
        JUnitAppender.suppressErrorMessage("Portal elem = null");

        WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);

        _OBlockMgr = InstanceManager.getDefault(OBlockManager.class);
        _sensorMgr = InstanceManager.getDefault(SensorManager.class);

        Sensor sensor3 = _sensorMgr.getBySystemName("IS3");
        assertNotNull( sensor3, "Senor IS3 not found");

        NXFrameTest.setAndConfirmSensorAction(sensor3, Sensor.ACTIVE, _OBlockMgr.getBySystemName("OB3"));

        WarrantTableFrame tableFrame = WarrantTableFrame.getDefault();
        assertNotNull( tableFrame, "tableFrame");

        Warrant warrant = tableFrame.getModel().getWarrantAt(0);
        assertNotNull( warrant, "warrant");

        tableFrame.runTrain(warrant, Warrant.MODE_RUN);
        JUnitUtil.waitFor(() -> {
            String m =  warrant.getRunningMessage();
            if ( m == null ) {
                return false;
            }
            return m.endsWith("Cmd #3.");
        }, "Train is moving at 3rd command");

       // OBlock sensor names
        String[] route = {"OB3", "OB4", "OB5", "OB10", "OB0", "OB1", "OB2", "OB3"};
        OBlock block = _OBlockMgr.getOBlock("OB3");
        // runtimes() in next line runs the train, then checks location
        assertEquals( block.getSensor().getDisplayName(),
            runtimes(route, _OBlockMgr).getDisplayName(), "Train in last block");

        // passed test - cleanup.  Do it here so failure leaves traces.
        JFrameOperator jfo = new JFrameOperator(tableFrame);
        jfo.requestClose();
        jfo.waitClosed();
        // we may want to use jemmy to close the panel as well.
        ControlPanelEditor panel = (ControlPanelEditor) jmri.util.JmriJFrame.getFrame("NXWarrantTest");
        // disposing this way allows test to be rerun (i.e. reload panel file) multiple times
        Boolean retVal = jmri.util.ThreadingUtil.runOnGUIwithReturn(() -> {
            panel.dispose();
            return true;
        });
        assertTrue(retVal);

        JUnitUtil.waitThreadTerminated("Loop Killer");

    }

    @Test
    @DisabledIfSystemProperty(named ="jmri.skipTestsRequiringSeparateRunning", matches ="true")
    public void testWarrantRampHalt() throws Exception {
        // load and display
        File f = new File("java/test/jmri/jmrit/logix/valid/NXWarrantTest.xml");
        InstanceManager.getDefault(ConfigureManager.class).load(f);
        JUnitAppender.suppressErrorMessage("Portal elem = null");

        WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);

        _OBlockMgr = InstanceManager.getDefault(OBlockManager.class);
        _sensorMgr = InstanceManager.getDefault(SensorManager.class);

        Sensor sensor1 = _sensorMgr.getBySystemName("IS1");
        assertNotNull( sensor1, "Senor IS1 not found");

        NXFrameTest.setAndConfirmSensorAction(sensor1, Sensor.ACTIVE, _OBlockMgr.getBySystemName("OB1"));

        WarrantTableFrame tableFrame = WarrantTableFrame.getDefault();
        assertNotNull( tableFrame, "tableFrame");

        Warrant warrant = tableFrame.getModel().getWarrantAt(1);
        assertNotNull( warrant, "warrant");

        tableFrame.runTrain(warrant, Warrant.MODE_RUN);

        SpeedUtil sp = warrant.getSpeedUtil();
        sp.setRampThrottleIncrement(0.15f);
        sp.setRampTimeIncrement(100);

        JUnitUtil.waitFor(() -> {
            String m =  warrant.getRunningMessage();
            if ( m == null ) {
                return false;
            }
            return m.endsWith("Cmd #8.");
        }, "Train starts to move at 8th command");

       // OBlock sensor names
        String[] route1 = {"OB1", "OB6", "OB3"};
        final OBlock block3 = _OBlockMgr.getOBlock("OB3");
        // runtimes() in next line runs the train, then checks location
        assertEquals( block3.getSensor().getDisplayName(),
            runtimes(route1,_OBlockMgr).getDisplayName(), "Train in block OB3");

        warrant.controlRunTrain(Warrant.HALT); // user interrupts script
        JUnitUtil.waitFor(100);     // waitEmpty(10) causes a lot of failures on Travis GUI
        JUnitUtil.waitFor(() -> {
            String m =  warrant.getRunningMessage();
            return (m.equals(Bundle.getMessage("RampHalt", warrant.getTrainName(), block3.getDisplayName())));
        }, "Train Halted");

        warrant.controlRunTrain(Warrant.RESUME);

        JUnitUtil.waitFor(() -> {
            String m =  warrant.getRunningMessage();
            if ( m == null ) {
                return false;
            }
            return m.startsWith("At speed Normal") ||
                    m.startsWith("Overdue for arrival at block");
        }, "Train Resumed");

        String[] route2 = {"OB3", "OB7", "OB5"};
        OBlock block5 = _OBlockMgr.getOBlock("OB5");
        // runtimes() in next line runs the train, then checks location
        assertEquals( block5.getSensor().getDisplayName(),
            runtimes(route2, _OBlockMgr).getDisplayName(), "Train in last block");

        // passed test - cleanup.  Do it here so failure leaves traces.
        JFrameOperator jfo = new JFrameOperator(tableFrame);
        jfo.requestClose();
        jfo.waitClosed();
        // we may want to use jemmy to close the panel as well.
        ControlPanelEditor panel = (ControlPanelEditor) jmri.util.JmriJFrame.getFrame("NXWarrantTest");
        // disposing this way allows test to be rerun (i.e. reload panel file) multiple times
        Boolean retVal = jmri.util.ThreadingUtil.runOnGUIwithReturn(() -> {
            panel.dispose();
            return true;
        });
        assertTrue(retVal);

        JUnitUtil.waitThreadTerminated("RampHalt Killer");

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
        assertTrue( idx > 0 && idx < route.length , "Index "+ idx + " invalid ");

        OBlock fromBlock = mgr.getOBlock(route[idx - 1]);
        Sensor fromSensor = fromBlock.getSensor();
        assertNotNull( fromSensor, "fromSensor not found");

        OBlock toBlock = mgr.getOBlock(route[idx]);
        Sensor toSensor = toBlock.getSensor();
        assertNotNull( toSensor, "toSensor not found");

        JUnitUtil.waitFor(300);
        NXFrameTest.setAndConfirmSensorAction(toSensor, Sensor.ACTIVE, toBlock);

        JUnitUtil.waitFor(200);
        NXFrameTest.setAndConfirmSensorAction(fromSensor, Sensor.INACTIVE, fromBlock);

        return toSensor;
    }


    protected static void setAndConfirmSensorAction(Sensor sensor, int state, OBlock block)  {
        if (state == Sensor.ACTIVE) {

            ThreadingUtil.runOnLayout(() -> {
                assertDoesNotThrow(() -> sensor.setState(Sensor.ACTIVE),
                    "Set "+ sensor.getDisplayName()+" ACTIVE Exception: ");
            });
            OBlock b = block;
            JUnitUtil.waitFor(() -> {
                return (b.getState() & OBlock.OCCUPIED) != 0;
            }, b.getDisplayName() + " occupied");
        } else if (state == Sensor.INACTIVE) {
            ThreadingUtil.runOnLayout(() -> {
                assertDoesNotThrow(() -> sensor.setState(Sensor.INACTIVE),
                    "Set "+ sensor.getDisplayName()+" INACTIVE Exception: ");
            });
            OBlock b = block;
            JUnitUtil.waitFor(() -> {
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
        InstanceManager.getDefault(WarrantManager.class).dispose();
        JUnitUtil.resetWindows(false,false);
        JUnitUtil.tearDown();
    }

}
