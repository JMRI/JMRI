package jmri.jmrit.logix;

import java.io.File;
import java.util.List;

import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.Sensor;
import jmri.jmrit.display.controlPanelEditor.ControlPanelEditor;
import jmri.util.JmriJFrame;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JFrameOperator;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class TrackerTableActionTest {

    private OBlockManager _OBlockMgr;

    /**
     * Checks automatic creation
     */
    @Test
    public void testInstance() {
        TrackerTableAction t = InstanceManager.getDefault(TrackerTableAction.class);
        assertNotNull(t, "exists");
    }

    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testTracking1() throws Exception {

        Assumptions.assumeFalse(Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"), "Ignoring intermittent test");
        WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);

        // load and display
        File f = new File("java/test/jmri/jmrit/logix/valid/IndicatorDemoTest.xml");
        InstanceManager.getDefault(ConfigureManager.class).load(f);
        JUnitAppender.suppressErrorMessage("Portal elem = null");

        ControlPanelEditor panel = (ControlPanelEditor) JmriJFrame.getFrame("Indicator Demo 1 Editor");
        assertNotNull(panel);
        panel.setVisible(false);

        TrackerTableAction tta = InstanceManager.getDefault(TrackerTableAction.class);
        assertNotNull(tta, "TrackerTableAction not found");

        OBlock middle = _OBlockMgr.getByUserName("Middle");
        assertNotNull(middle);
        Sensor sMiddle = middle.getSensor();
        assertNotNull(sMiddle, "Senor sMiddle not found");
        NXFrameTest.setAndConfirmSensorAction(sMiddle, Sensor.ACTIVE, middle);
        JUnitUtil.waitFor(() -> (middle.getState() & OBlock.OCCUPIED) != 0, "Middle occupied");

        tta.markNewTracker(middle, "Tkr1", null);
        Tracker tracker1 = tta.findTrackerIn(middle);
        assertNotNull(tta, "Tracker Tkr1 found");
        List<OBlock> occupied = tracker1.getBlocksOccupied();
        assertEquals(1, occupied.size(),"Tkr1 Blocks Occupied");

        OBlock farEast = _OBlockMgr.getByUserName("FarEast");
        assertNotNull(farEast);
        Sensor sFarEast = farEast.getSensor();
        assertNotNull(sFarEast, "Senor FarEast not found");
        NXFrameTest.setAndConfirmSensorAction(sFarEast, Sensor.ACTIVE, farEast);
        occupied = tracker1.getBlocksOccupied();
        assertEquals(2, occupied.size(), "Tkr1 2 Blocks Occupied");

        OBlock east = _OBlockMgr.getByUserName("East");
        assertNotNull(east);
        Sensor sEast = east.getSensor();
        NXFrameTest.setAndConfirmSensorAction(sEast, Sensor.ACTIVE, east);
        occupied = tracker1.getBlocksOccupied();
        assertEquals(3, occupied.size(),"Tkr1 3 Blocks Occupied");

        NXFrameTest.setAndConfirmSensorAction(sMiddle, Sensor.INACTIVE, middle);
        occupied = tracker1.getBlocksOccupied();
        assertEquals(2, occupied.size(),"Tkr1 2 Blocks Occupied");


        NXFrameTest.setAndConfirmSensorAction(sFarEast, Sensor.INACTIVE, farEast);
        occupied = tracker1.getBlocksOccupied();
        assertEquals(1, occupied.size(), "Tkr1 1 Blocks Occupied");

        tta.stopTracker(tracker1, east);
        

        // JFrameOperator requestClose just hides the Tracker Table, not disposing of it.
        Boolean retVal = ThreadingUtil.runOnGUIwithReturn(() -> {
            panel.dispose();
            JmriJFrame.getFrame(Bundle.getMessage("TrackerTable")).dispose();
            return true;
        });
        assertTrue(retVal);

    }

    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testTrackingDark() throws Exception {
        Assumptions.assumeFalse(Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"), "Ignoring intermittent test");
        WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);

        // load and display
        File f = new File("java/test/jmri/jmrit/logix/valid/IndicatorDemoTest.xml");
        InstanceManager.getDefault(ConfigureManager.class).load(f);
        JUnitAppender.suppressErrorMessage("Portal elem = null");

        ControlPanelEditor panel = (ControlPanelEditor) JmriJFrame.getFrame("Indicator Demo 1 Editor");
        assertNotNull(panel);
        panel.setVisible(false);

        TrackerTableAction tta = InstanceManager.getDefault(TrackerTableAction.class);
        assertNotNull(tta, "TrackerTableAction not found");

        OBlock mainBlock = _OBlockMgr.getByUserName("Main");
        assertNotNull(mainBlock);
        Sensor sMain = mainBlock.getSensor();
        assertNotNull(sMain,"Senor sMain found");
        NXFrameTest.setAndConfirmSensorAction(sMain, Sensor.ACTIVE, mainBlock);
        tta.markNewTracker(mainBlock, "Tkr1", null);
        Tracker trackerD = tta.findTrackerIn(mainBlock);
        assertNotNull(trackerD, "Tracker TkrD not found");
        List<OBlock> occupied = trackerD.getBlocksOccupied();
        assertEquals(1, occupied.size(), "TkrD Blocks Occupied");

        // passes through dark block "West Yard"
        OBlock farWestBlock = _OBlockMgr.getByUserName("FarWest");
        assertNotNull(farWestBlock);
        Sensor sFarWest = farWestBlock.getSensor();
        NXFrameTest.setAndConfirmSensorAction(sFarWest, Sensor.ACTIVE, farWestBlock);
        occupied = trackerD.getBlocksOccupied();
        assertEquals(3, occupied.size(), "TkrD 3 Blocks Occupied");

        NXFrameTest.setAndConfirmSensorAction(sMain, Sensor.INACTIVE, mainBlock);
        occupied = trackerD.getBlocksOccupied();
        assertEquals(1, occupied.size(),"TkrD 1 Blocks Occupied");

        tta.stopTracker(trackerD, farWestBlock);
        

        // JFrameOperator requestClose just hides the Tracker Table, not disposing of it.
        Boolean retVal = ThreadingUtil.runOnGUIwithReturn(() -> {
            panel.dispose();
            JmriJFrame.getFrame(Bundle.getMessage("TrackerTable")).dispose();
            return true;
        });
        assertTrue(retVal);

    }

    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testMultipleTrackers() throws Exception {

        Assumptions.assumeFalse(Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"), "Ignoring intermittent test");
        WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);

        // load and display
        File f = new File("java/test/jmri/jmrit/logix/valid/IndicatorDemoTest.xml");
        InstanceManager.getDefault(ConfigureManager.class).load(f);
        JUnitAppender.suppressErrorMessage("Portal elem = null");

        ControlPanelEditor panel = (ControlPanelEditor) JmriJFrame.getFrame("Indicator Demo 1 Editor");
        assertNotNull(panel);
        panel.setVisible(false);

        TrackerTableAction tta = InstanceManager.getDefault(TrackerTableAction.class);
        assertNotNull(tta, "TrackerTableAction not found");

        OBlock westBlock = _OBlockMgr.getByUserName("West");
        assertNotNull(westBlock);
        Sensor sWest = westBlock.getSensor();
        assertNotNull(sWest, "Senor sWest found");
        NXFrameTest.setAndConfirmSensorAction(sWest, Sensor.ACTIVE, westBlock);
        tta.markNewTracker(westBlock, "TkrW", null);
        Tracker tkrW = tta.findTrackerIn(westBlock);
        assertNotNull(tkrW, "Tracker TkrW found");

        OBlock eastBlock = _OBlockMgr.getByUserName("East");
        assertNotNull(eastBlock);
        Sensor sEast = eastBlock.getSensor();
        assertNotNull(sEast, "Senor sEast found");
        NXFrameTest.setAndConfirmSensorAction(sEast, Sensor.ACTIVE, eastBlock);
        tta.markNewTracker(eastBlock, "TkrE", null);
        Tracker TkrE = tta.findTrackerIn(eastBlock);
        assertNotNull(TkrE, "Tracker TkrE found");

        OBlock mainBlock = _OBlockMgr.getByUserName("Main");
        assertNotNull(mainBlock);
        Sensor sMain = mainBlock.getSensor();
        assertNotNull(sMain, "Senor sMain found");
        NXFrameTest.setAndConfirmSensorAction(sMain, Sensor.ACTIVE, mainBlock);

        JFrameOperator nfo = new JFrameOperator(tta._frame);
        JDialogOperator jdo = new JDialogOperator(nfo, Bundle.getMessage("TrackerTitle"));
        assertNotNull(jdo, "Dialog operator found");

        TrackerTableAction.ChooseTracker dialog = (TrackerTableAction.ChooseTracker)jdo.getSource();
        assertNotNull(dialog, "JDialog found");
        // first entry ought to be tracker "West"
        dialog._jList.setSelectedIndex(0);

        List<OBlock> occupied = TkrE.getBlocksOccupied();
        assertEquals(1, occupied.size(), "TkrE Blocks Occupied");

        occupied = tkrW.getBlocksOccupied();
        assertEquals(2, occupied.size(), "TkrW Blocks Occupied");


        // JFrameOperator requestClose just hides the Tracker Table, not disposing of it.
        Boolean retVal = ThreadingUtil.runOnGUIwithReturn(() -> {
            panel.dispose();
            JmriJFrame.getFrame(Bundle.getMessage("TrackerTable")).dispose();
            return true;
        });
        assertTrue(retVal);

    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initOBlockManager();
        JUnitUtil.initDebugThrottleManager();
        _OBlockMgr = InstanceManager.getDefault(OBlockManager.class);
    }

    @AfterEach
    public void tearDown() {
        _OBlockMgr.dispose();
        _OBlockMgr = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
