package jmri.jmrit.logix;

import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.Sensor;
import jmri.jmrit.display.controlPanelEditor.ControlPanelEditor;
import jmri.util.JUnitUtil;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JFrameOperator;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class TrackerTableActionTest {

    OBlockManager _OBlockMgr;

    /**
     * Checks automatic creation
     */
    @Test
    public void testInstance() {
        TrackerTableAction t = jmri.InstanceManager.getDefault(TrackerTableAction.class);
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testTracking1() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assume.assumeFalse("Ignoring intermittent test", Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"));
        WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);

        // load and display
        File f = new File("java/test/jmri/jmrit/logix/valid/IndicatorDemoTest.xml");
        InstanceManager.getDefault(ConfigureManager.class).load(f);
        ControlPanelEditor panel = (ControlPanelEditor) jmri.util.JmriJFrame.getFrame("Indicator Demo 0 Editor");
        panel.setVisible(false);

        TrackerTableAction tta = jmri.InstanceManager.getDefault(TrackerTableAction.class);
        Assert.assertNotNull("TrackerTableAction not found", tta);

        OBlock Middle = _OBlockMgr.getByUserName("Middle");
        Sensor sMiddle = Middle.getSensor();
        Assert.assertNotNull("Senor sMiddle not found", sMiddle);
        NXFrameTest.setAndConfirmSensorAction(sMiddle, Sensor.ACTIVE, Middle);
        jmri.util.JUnitUtil.waitFor(() -> {
            return (Middle.getState() & OBlock.OCCUPIED) != 0;
        }, "Middle occupied");

        tta.markNewTracker(Middle, "Tkr1", null);
        Tracker Tkr1 = tta.findTrackerIn(Middle);
        Assert.assertNotNull("Tracker Tkr1 found", tta);
        List<OBlock> occupied = Tkr1.getBlocksOccupied();
        Assert.assertEquals("Tkr1 Blocks Occupied", 1, occupied.size());
        
        OBlock FarEast = _OBlockMgr.getByUserName("FarEast");
        Sensor sFarEast = FarEast.getSensor();
        Assert.assertNotNull("Senor FarEast not found", sFarEast);
        NXFrameTest.setAndConfirmSensorAction(sFarEast, Sensor.ACTIVE, FarEast);
        occupied = Tkr1.getBlocksOccupied();
        Assert.assertEquals("Tkr1 2 Blocks Occupied", 2, occupied.size());

        OBlock East = _OBlockMgr.getByUserName("East");
        Sensor sEast = East.getSensor();
        NXFrameTest.setAndConfirmSensorAction(sEast, Sensor.ACTIVE, East);
        occupied = Tkr1.getBlocksOccupied();
        Assert.assertEquals("Tkr1 3 Blocks Occupied", 3, occupied.size());
        
        NXFrameTest.setAndConfirmSensorAction(sMiddle, Sensor.INACTIVE, Middle);
        occupied = Tkr1.getBlocksOccupied();
        Assert.assertEquals("Tkr1 2 Blocks Occupied", 2, occupied.size());

        
        NXFrameTest.setAndConfirmSensorAction(sFarEast, Sensor.INACTIVE, FarEast);
        occupied = Tkr1.getBlocksOccupied();
        Assert.assertEquals("Tkr1 1 Blocks Occupied", 1, occupied.size());

        tta.stopTracker(Tkr1, East);
        panel.dispose();
    }

    @Test
    public void testTrackingDark() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assume.assumeFalse("Ignoring intermittent test", Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"));
        WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);

        // load and display
        File f = new File("java/test/jmri/jmrit/logix/valid/IndicatorDemoTest.xml");
        InstanceManager.getDefault(ConfigureManager.class).load(f);
        ControlPanelEditor panel = (ControlPanelEditor) jmri.util.JmriJFrame.getFrame("Indicator Demo 0 Editor");
        panel.setVisible(false);

        TrackerTableAction tta = jmri.InstanceManager.getDefault(TrackerTableAction.class);
        Assert.assertNotNull("TrackerTableAction not found", tta);

        OBlock Main = _OBlockMgr.getByUserName("Main");
        Sensor sMain = Main.getSensor();
        Assert.assertNotNull("Senor sMain found", sMain);
        NXFrameTest.setAndConfirmSensorAction(sMain, Sensor.ACTIVE, Main);
        tta.markNewTracker(Main, "Tkr1", null);
        Tracker TkrD = tta.findTrackerIn(Main);
        Assert.assertNotNull("Tracker TkrD not found", TkrD);
        List<OBlock> occupied = TkrD.getBlocksOccupied();
        Assert.assertEquals("TkrD Blocks Occupied", 1, occupied.size());

        // passes through dark block "West Yard"
        OBlock FarWest = _OBlockMgr.getByUserName("FarWest");
        Sensor sFarWest = FarWest.getSensor();
        NXFrameTest.setAndConfirmSensorAction(sFarWest, Sensor.ACTIVE, FarWest);
        occupied = TkrD.getBlocksOccupied();
        Assert.assertEquals("TkrD 3 Blocks Occupied", 3, occupied.size());
        
        NXFrameTest.setAndConfirmSensorAction(sMain, Sensor.INACTIVE, Main);
        occupied = TkrD.getBlocksOccupied();
        Assert.assertEquals("TkrD 1 Blocks Occupied", 1, occupied.size());

        tta.stopTracker(TkrD, FarWest);
        panel.dispose();
    }

    @Test
    public void testMultipleTrackers() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assume.assumeFalse("Ignoring intermittent test", Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"));
        WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);

        // load and display
        File f = new File("java/test/jmri/jmrit/logix/valid/IndicatorDemoTest.xml");
        InstanceManager.getDefault(ConfigureManager.class).load(f);
        ControlPanelEditor panel = (ControlPanelEditor) jmri.util.JmriJFrame.getFrame("Indicator Demo 0 Editor");
        panel.setVisible(false);

        TrackerTableAction tta = jmri.InstanceManager.getDefault(TrackerTableAction.class);
        Assert.assertNotNull("TrackerTableAction not found", tta);

        OBlock West = _OBlockMgr.getByUserName("West");
        Sensor sWest = West.getSensor();
        Assert.assertNotNull("Senor sWest found", sWest);
        NXFrameTest.setAndConfirmSensorAction(sWest, Sensor.ACTIVE, West);
        tta.markNewTracker(West, "TkrW", null);
        Tracker TkrW = tta.findTrackerIn(West);
        Assert.assertNotNull("Tracker TkrW found", TkrW);
        
        OBlock East = _OBlockMgr.getByUserName("East");
        Sensor sEast = East.getSensor();
        Assert.assertNotNull("Senor sEast found", sEast);
        NXFrameTest.setAndConfirmSensorAction(sEast, Sensor.ACTIVE, East);
        tta.markNewTracker(East, "TkrE", null);
        Tracker TkrE = tta.findTrackerIn(East);
        Assert.assertNotNull("Tracker TkrE found", TkrE);

        OBlock Main = _OBlockMgr.getByUserName("Main");
        Sensor sMain = Main.getSensor();
        Assert.assertNotNull("Senor sMain found", sMain);
        NXFrameTest.setAndConfirmSensorAction(sMain, Sensor.ACTIVE, Main);
        
        JFrameOperator nfo = new JFrameOperator(tta._frame);
        JDialogOperator jdo = new JDialogOperator(nfo, Bundle.getMessage("TrackerTitle"));
        Assert.assertNotNull("Dialog operator found", jdo);
//      JComponentOperator jco = new JComponentOperator(jdo);
        
        TrackerTableAction.ChooseTracker dialog = (TrackerTableAction.ChooseTracker)jdo.getSource();
        Assert.assertNotNull("JDialog found", dialog);
        // first entry ought to be tracker "West"
        dialog._jList.setSelectedIndex(0);

        List<OBlock> occupied = TkrE.getBlocksOccupied();
        Assert.assertEquals("TkrE Blocks Occupied", 1, occupied.size());
        
        occupied = TkrW.getBlocksOccupied();
        Assert.assertEquals("TkrW Blocks Occupied", 2, occupied.size());

        panel.dispose();
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initOBlockManager();
        JUnitUtil.initDebugThrottleManager();
        _OBlockMgr = InstanceManager.getDefault(OBlockManager.class);
    }

    @After
    public void tearDown() {
        _OBlockMgr.dispose();
        _OBlockMgr = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(TrackerTableActionTest.class);

}
