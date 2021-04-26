package jmri.jmrit.logix;

import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.Sensor;
import jmri.jmrit.display.controlPanelEditor.ControlPanelEditor;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.util.List;

import org.junit.jupiter.api.*;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JFrameOperator;

import static org.assertj.core.api.Assertions.assertThat;

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
        assertThat(t).withFailMessage("exists").isNotNull();
    }

    @Test
    public void testTracking1() throws Exception {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());
        Assumptions.assumeFalse(Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"), "Ignoring intermittent test");
        WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);

        // load and display
        File f = new File("java/test/jmri/jmrit/logix/valid/IndicatorDemoTest.xml");
        InstanceManager.getDefault(ConfigureManager.class).load(f);
        jmri.util.JUnitAppender.suppressErrorMessage("Portal elem = null");
        // clear IconFamily warning, fixed in 4.21.5
        // fixed: JUnitAppender.assertWarnMessage("getIconMap failed. family \"null\" not found in item type \"Portal\"");
        ControlPanelEditor panel = (ControlPanelEditor) jmri.util.JmriJFrame.getFrame("Indicator Demo 1 Editor");
        assert panel != null;
        panel.setVisible(false);

        TrackerTableAction tta = jmri.InstanceManager.getDefault(TrackerTableAction.class);
        assertThat(tta).withFailMessage("TrackerTableAction not found").isNotNull();

        OBlock Middle = _OBlockMgr.getByUserName("Middle");
        assert Middle != null;
        Sensor sMiddle = Middle.getSensor();
        assertThat(sMiddle).withFailMessage("Senor sMiddle not found").isNotNull();
        NXFrameTest.setAndConfirmSensorAction(sMiddle, Sensor.ACTIVE, Middle);
        jmri.util.JUnitUtil.waitFor(() -> {
            return (Middle.getState() & OBlock.OCCUPIED) != 0;
        }, "Middle occupied");

        tta.markNewTracker(Middle, "Tkr1", null);
        Tracker Tkr1 = tta.findTrackerIn(Middle);
        assertThat(tta).withFailMessage("Tracker Tkr1 found").isNotNull();
        List<OBlock> occupied = Tkr1.getBlocksOccupied();
        assertThat(occupied.size()).withFailMessage("Tkr1 Blocks Occupied").isEqualTo(1);

        OBlock FarEast = _OBlockMgr.getByUserName("FarEast");
        assert FarEast != null;
        Sensor sFarEast = FarEast.getSensor();
        assertThat(sFarEast).withFailMessage("Senor FarEast not found").isNotNull();
        NXFrameTest.setAndConfirmSensorAction(sFarEast, Sensor.ACTIVE, FarEast);
        occupied = Tkr1.getBlocksOccupied();
        assertThat(occupied.size()).withFailMessage("Tkr1 2 Blocks Occupied").isEqualTo(2);

        OBlock East = _OBlockMgr.getByUserName("East");
        assert East != null;
        Sensor sEast = East.getSensor();
        NXFrameTest.setAndConfirmSensorAction(sEast, Sensor.ACTIVE, East);
        occupied = Tkr1.getBlocksOccupied();
        assertThat(occupied.size()).withFailMessage("Tkr1 3 Blocks Occupied").isEqualTo(3);

        NXFrameTest.setAndConfirmSensorAction(sMiddle, Sensor.INACTIVE, Middle);
        occupied = Tkr1.getBlocksOccupied();
        assertThat(occupied.size()).withFailMessage("Tkr1 2 Blocks Occupied").isEqualTo(2);


        NXFrameTest.setAndConfirmSensorAction(sFarEast, Sensor.INACTIVE, FarEast);
        occupied = Tkr1.getBlocksOccupied();
        assertThat(occupied.size()).withFailMessage("Tkr1 1 Blocks Occupied").isEqualTo(1);

        tta.stopTracker(Tkr1, East);
        panel.dispose();
    }

    @Test
    public void testTrackingDark() throws Exception {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());
        Assumptions.assumeFalse(Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"), "Ignoring intermittent test");
        WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);

        // load and display
        File f = new File("java/test/jmri/jmrit/logix/valid/IndicatorDemoTest.xml");
        InstanceManager.getDefault(ConfigureManager.class).load(f);
        jmri.util.JUnitAppender.suppressErrorMessage("Portal elem = null");
        // clear IconFamily warning, fixed in 4.21.5
        // fixed: JUnitAppender.assertWarnMessage("getIconMap failed. family \"null\" not found in item type \"Portal\"");
        ControlPanelEditor panel = (ControlPanelEditor) jmri.util.JmriJFrame.getFrame("Indicator Demo 1 Editor");
        assert panel != null;
        panel.setVisible(false);

        TrackerTableAction tta = jmri.InstanceManager.getDefault(TrackerTableAction.class);
        assertThat(tta).withFailMessage("TrackerTableAction not found").isNotNull();

        OBlock Main = _OBlockMgr.getByUserName("Main");
        assert Main != null;
        Sensor sMain = Main.getSensor();
        assertThat(sMain).withFailMessage("Senor sMain found").isNotNull();
        NXFrameTest.setAndConfirmSensorAction(sMain, Sensor.ACTIVE, Main);
        tta.markNewTracker(Main, "Tkr1", null);
        Tracker TkrD = tta.findTrackerIn(Main);
        assertThat(TkrD).withFailMessage("Tracker TkrD not found").isNotNull();
        List<OBlock> occupied = TkrD.getBlocksOccupied();
        assertThat(occupied.size()).withFailMessage("TkrD Blocks Occupied").isEqualTo(1);

        // passes through dark block "West Yard"
        OBlock FarWest = _OBlockMgr.getByUserName("FarWest");
        assert FarWest != null;
        Sensor sFarWest = FarWest.getSensor();
        NXFrameTest.setAndConfirmSensorAction(sFarWest, Sensor.ACTIVE, FarWest);
        occupied = TkrD.getBlocksOccupied();
        assertThat(occupied.size()).withFailMessage("TkrD 3 Blocks Occupied").isEqualTo(3);

        NXFrameTest.setAndConfirmSensorAction(sMain, Sensor.INACTIVE, Main);
        occupied = TkrD.getBlocksOccupied();
        assertThat(occupied.size()).withFailMessage("TkrD 1 Blocks Occupied").isEqualTo(1);

        tta.stopTracker(TkrD, FarWest);
        panel.dispose();
    }

    @Test
    public void testMultipleTrackers() throws Exception {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());
        Assumptions.assumeFalse(Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"), "Ignoring intermittent test");
        WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);

        // load and display
        File f = new File("java/test/jmri/jmrit/logix/valid/IndicatorDemoTest.xml");
        InstanceManager.getDefault(ConfigureManager.class).load(f);
        jmri.util.JUnitAppender.suppressErrorMessage("Portal elem = null");
        // clear IconFamily warning, fixed in 4.21.5
        // fixed: JUnitAppender.assertWarnMessage("getIconMap failed. family \"null\" not found in item type \"Portal\"");
        ControlPanelEditor panel = (ControlPanelEditor) jmri.util.JmriJFrame.getFrame("Indicator Demo 1 Editor");
        assert panel != null;
        panel.setVisible(false);

        TrackerTableAction tta = jmri.InstanceManager.getDefault(TrackerTableAction.class);
        assertThat(tta).withFailMessage("TrackerTableAction not found").isNotNull();

        OBlock West = _OBlockMgr.getByUserName("West");
        assert West != null;
        Sensor sWest = West.getSensor();
        assertThat(sWest).withFailMessage("Senor sWest found").isNotNull();
        NXFrameTest.setAndConfirmSensorAction(sWest, Sensor.ACTIVE, West);
        tta.markNewTracker(West, "TkrW", null);
        Tracker TkrW = tta.findTrackerIn(West);
        assertThat(TkrW).withFailMessage("Tracker TkrW found").isNotNull();

        OBlock East = _OBlockMgr.getByUserName("East");
        assert East != null;
        Sensor sEast = East.getSensor();
        assertThat(sEast).withFailMessage("Senor sEast found").isNotNull();
        NXFrameTest.setAndConfirmSensorAction(sEast, Sensor.ACTIVE, East);
        tta.markNewTracker(East, "TkrE", null);
        Tracker TkrE = tta.findTrackerIn(East);
        assertThat(TkrE).withFailMessage("Tracker TkrE found").isNotNull();

        OBlock Main = _OBlockMgr.getByUserName("Main");
        assert Main != null;
        Sensor sMain = Main.getSensor();
        assertThat(sMain).withFailMessage("Senor sMain found").isNotNull();
        NXFrameTest.setAndConfirmSensorAction(sMain, Sensor.ACTIVE, Main);

        JFrameOperator nfo = new JFrameOperator(tta._frame);
        JDialogOperator jdo = new JDialogOperator(nfo, Bundle.getMessage("TrackerTitle"));
        assertThat(jdo).withFailMessage("Dialog operator found").isNotNull();
//      JComponentOperator jco = new JComponentOperator(jdo);

        TrackerTableAction.ChooseTracker dialog = (TrackerTableAction.ChooseTracker)jdo.getSource();
        assertThat(dialog).withFailMessage("JDialog found").isNotNull();
        // first entry ought to be tracker "West"
        dialog._jList.setSelectedIndex(0);

        List<OBlock> occupied = TkrE.getBlocksOccupied();
        assertThat(occupied.size()).withFailMessage("TkrE Blocks Occupied").isEqualTo(1);

        occupied = TkrW.getBlocksOccupied();
        assertThat(occupied.size()).withFailMessage("TkrW Blocks Occupied").isEqualTo(2);

        panel.dispose();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
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
        JUnitUtil.deregisterEditorManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(TrackerTableActionTest.class);

}
