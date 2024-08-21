package jmri.jmrit.logix;

import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.Sensor;
import jmri.jmrit.display.EditorScaffold;
import jmri.jmrit.display.LocoIcon;
import jmri.jmrit.display.controlPanelEditor.ControlPanelEditor;
import jmri.util.JUnitUtil;

import java.io.File;
import java.util.List;

import jmri.util.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JFrameOperator;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
@DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
public class TrackerTest {

    @Test
    public void testCTor() {
        Tracker t = new Tracker(new OBlock("OB1", "Test"), "Test",
                new LocoIcon(new EditorScaffold()),
                InstanceManager.getDefault(TrackerTableAction.class));
        assertNotNull(t,"exists");

        // JFrameOperator requestClose just hides the Tracker Table, not disposing of it.
        Boolean retVal = ThreadingUtil.runOnGUIwithReturn(() -> {
            JmriJFrame.getFrame(Bundle.getMessage("TrackerTable")).dispose();
            return true;
        });
        assertTrue(retVal);

    }

    @Test
    public void testTrack() {
        TrackerTableAction trackTable = InstanceManager.getDefault(TrackerTableAction.class);
        OBlock blk1 = new OBlock("OB1", "blk1");
        blk1.setState(OBlock.OCCUPIED);
        Tracker t = new Tracker(blk1, "Test",
                new LocoIcon(new EditorScaffold()),
                trackTable);
        assertNotNull(t,"exists");
        List<OBlock> occupied = t.getBlocksOccupied();
        assertEquals(1, occupied.size(), "Number Blocks Occupied");

        // JFrameOperator requestClose just hides the Tracker Table, not disposing of it.
        Boolean retVal = ThreadingUtil.runOnGUIwithReturn(() -> {
            JmriJFrame.getFrame(Bundle.getMessage("TrackerTable")).dispose();
            return true;
        });
        assertTrue(retVal);
    }

    @Test
    public void testMultipleStartBlocks() throws Exception {
        Assumptions.assumeFalse(Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"), "Ignoring intermittent test");
        WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);

        // load and display
        File f = new File("java/test/jmri/jmrit/logix/valid/IndicatorDemoTest.xml");
        InstanceManager.getDefault(ConfigureManager.class).load(f);
        JUnitAppender.suppressErrorMessage("Portal elem = null");

        TrackerTableAction tta = InstanceManager.getDefault(TrackerTableAction.class);
        assertNotNull(tta,"TrackerTableAction not found");
        OBlockManager _OBlockMgr = InstanceManager.getDefault(OBlockManager.class);

        OBlock westBlock = _OBlockMgr.getByUserName("West");
        assertNotNull(westBlock);
        Sensor sWest = westBlock.getSensor();
        assertNotNull(sWest, "Sensor sWest found");
        NXFrameTest.setAndConfirmSensorAction(sWest, Sensor.ACTIVE, westBlock);

        OBlock mainBlock = _OBlockMgr.getByUserName("Main");
        assertNotNull(mainBlock);
        Sensor sMain = mainBlock.getSensor();
        assertNotNull(sMain, "Sensor sMain found");
        NXFrameTest.setAndConfirmSensorAction(sMain, Sensor.ACTIVE, mainBlock);

        OBlock eastBlock = _OBlockMgr.getByUserName("East");
        assertNotNull(eastBlock);
        Sensor sEast = eastBlock.getSensor();
        assertNotNull(sEast, "Sensor sEast found");
        NXFrameTest.setAndConfirmSensorAction(sEast, Sensor.ACTIVE, eastBlock);

        tta.actionPerformed(null);  // called to make TrackerTableAction._frame
        Tracker tkrWest = new Tracker(westBlock, "TkrFW", null, tta);
        assertNotNull(tkrWest, "Tracker TkrWest found");

        JFrameOperator nfo = new JFrameOperator(tta._frame);
        JDialogOperator jdo = new JDialogOperator(nfo, Bundle.getMessage("TrackerTitle"));
        assertNotNull(jdo, "Dialog operator found");

        Tracker.ChooseStartBlock dialog = (Tracker.ChooseStartBlock)jdo.getSource();
        assertNotNull(dialog, "JDialog found");

        dialog._jList.setSelectedIndex(0);

        List<OBlock> occupied = tkrWest.getBlocksOccupied();
        assertEquals(2, occupied.size(),"TkrWest Blocks Occupied");
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);

        dialog._jList.setSelectedIndex(0);

        occupied = tkrWest.getBlocksOccupied();
        assertEquals(3, occupied.size(),"TkrWest Blocks Occupied");


        ControlPanelEditor panel = (ControlPanelEditor) JmriJFrame.getFrame("Indicator Demo 1 Editor");

        // JFrameOperator requestClose just hides the Tracker Table, not disposing of it.
        Boolean retVal = ThreadingUtil.runOnGUIwithReturn(() -> {
            if (panel != null ) {
                panel.dispose();
            }
            JmriJFrame.getFrame(Bundle.getMessage("TrackerTable")).dispose();
            return true;
        });
        assertTrue(retVal);

        _OBlockMgr.dispose();

    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initOBlockManager();
        JUnitUtil.initDebugThrottleManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
