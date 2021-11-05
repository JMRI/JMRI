package jmri.jmrit.logix;

import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.Sensor;
import jmri.jmrit.display.EditorScaffold;
import jmri.jmrit.display.LocoIcon;
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
public class TrackerTest {
/*
    @Test
    public void testCTor() {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());
        Tracker t = new Tracker(new OBlock("OB1", "Test"), "Test",
                new LocoIcon(new EditorScaffold()),
                InstanceManager.getDefault(TrackerTableAction.class));
        assertThat(t).withFailMessage("exists").isNotNull();
    }

    @Test
    public void testTrack() {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());
        TrackerTableAction trackTable = InstanceManager.getDefault(TrackerTableAction.class);
        OBlock blk1 = new OBlock("OB1", "blk1");
        blk1.setState(OBlock.OCCUPIED);
        Tracker t = new Tracker(blk1, "Test",
                new LocoIcon(new EditorScaffold()),
                trackTable);
        assertThat(t).withFailMessage("exists").isNotNull();
        List<OBlock> occupied = t.getBlocksOccupied();
        assertThat(occupied.size()).withFailMessage("Number Blocks Occupied").isEqualTo(1);
    }

    @Test
    public void testMultipleStartBlocks() throws Exception {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());
        Assumptions.assumeFalse(Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"), "Ignoring intermittent test");
        WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);

        // load and display
        File f = new File("java/test/jmri/jmrit/logix/valid/IndicatorDemoTest.xml");
        InstanceManager.getDefault(ConfigureManager.class).load(f);
        jmri.util.JUnitAppender.suppressErrorMessage("Portal elem = null");
        // clear IconFamily warning, fixed in 4.21.5
        // fixed: JUnitAppender.assertWarnMessage("getIconMap failed. family \"null\" not found in item type \"Portal\"");

        TrackerTableAction tta = jmri.InstanceManager.getDefault(TrackerTableAction.class);
        assertThat(tta).withFailMessage("TrackerTableAction not found").isNotNull();
        OBlockManager _OBlockMgr = InstanceManager.getDefault(OBlockManager.class);

        OBlock West = _OBlockMgr.getByUserName("West");
        assert West != null;
        Sensor sWest = West.getSensor();
        assertThat(sWest).withFailMessage("Sensor sWest found").isNotNull();
        NXFrameTest.setAndConfirmSensorAction(sWest, Sensor.ACTIVE, West);

        OBlock Main = _OBlockMgr.getByUserName("Main");
        assert Main != null;
        Sensor sMain = Main.getSensor();
        assertThat(sMain).withFailMessage("Sensor sMain found").isNotNull();
        NXFrameTest.setAndConfirmSensorAction(sMain, Sensor.ACTIVE, Main);

        OBlock East = _OBlockMgr.getByUserName("East");
        assert East != null;
        Sensor sEast = East.getSensor();
        assertThat(sEast).withFailMessage("Sensor sEast found").isNotNull();
        NXFrameTest.setAndConfirmSensorAction(sEast, Sensor.ACTIVE, East);

        tta.actionPerformed(null);  // called to make TrackerTableAction._frame
        Tracker TkrWest = new Tracker(West, "TkrFW", null, tta);
        assertThat(TkrWest).withFailMessage("Tracker TkrWest found").isNotNull();

        JFrameOperator nfo = new JFrameOperator(tta._frame);
        JDialogOperator jdo = new JDialogOperator(nfo, Bundle.getMessage("TrackerTitle"));
        assertThat(jdo).withFailMessage("Dialog operator found").isNotNull();

        Tracker.ChooseStartBlock dialog = (Tracker.ChooseStartBlock)jdo.getSource();
        assertThat(dialog).withFailMessage("JDialog found").isNotNull();

        dialog._jList.setSelectedIndex(0);

        List<OBlock> occupied = TkrWest.getBlocksOccupied();
        assertThat(occupied.size()).withFailMessage("TkrWest Blocks Occupied").isEqualTo(2);
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);

        dialog._jList.setSelectedIndex(0);

        occupied = TkrWest.getBlocksOccupied();
        assertThat(occupied.size()).withFailMessage("TkrWest Blocks Occupied").isEqualTo(3);
//        jmri.util.JUnitAppender.assertErrorMessage("Sensor \"\" not found. from setNamedBean()");
        ControlPanelEditor panel = (ControlPanelEditor) jmri.util.JmriJFrame.getFrame("Indicator Demo 1 Editor");
        if (panel != null) {
            panel.dispose();
        }
        _OBlockMgr.dispose();
    }
*/
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initOBlockManager();
        JUnitUtil.initDebugThrottleManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.deregisterEditorManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(TrackerTest.class);

}
