package jmri.jmrit.logix;

import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.Sensor;
import jmri.jmrit.display.EditorScaffold;
import jmri.jmrit.display.LocoIcon;
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
public class TrackerTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Tracker t = new Tracker(new OBlock("OB1", "Test"), "Test", 
                new LocoIcon(new EditorScaffold()), 
                InstanceManager.getDefault(TrackerTableAction.class));
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testTrack() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        TrackerTableAction trackTable = InstanceManager.getDefault(TrackerTableAction.class);
        OBlock blk1 = new OBlock("OB1", "blk1");
        blk1.setState(OBlock.OCCUPIED);
        Tracker t = new Tracker(blk1, "Test", 
                new LocoIcon(new EditorScaffold()), 
                trackTable);
        Assert.assertNotNull("exists",t);
        List<OBlock> occupied = t.getBlocksOccupied();
        Assert.assertEquals("Number Blocks Occupied", 1, occupied.size());
    }

    @Test
    public void testMultipleStartBlocks() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assume.assumeFalse("Ignoring intermittent test", Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"));
        WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);

        // load and display
        File f = new File("java/test/jmri/jmrit/logix/valid/IndicatorDemoTest.xml");
        InstanceManager.getDefault(ConfigureManager.class).load(f);
        TrackerTableAction tta = jmri.InstanceManager.getDefault(TrackerTableAction.class);
        Assert.assertNotNull("TrackerTableAction not found", tta);
        OBlockManager _OBlockMgr = InstanceManager.getDefault(OBlockManager.class);
        
        OBlock West = _OBlockMgr.getByUserName("West");
        Sensor sWest = West.getSensor();
        Assert.assertNotNull("Senor sWest found", sWest);
        NXFrameTest.setAndConfirmSensorAction(sWest, Sensor.ACTIVE, West);

        OBlock Main = _OBlockMgr.getByUserName("Main");
        Sensor sMain = Main.getSensor();
        Assert.assertNotNull("Senor sWest found", sMain);
        NXFrameTest.setAndConfirmSensorAction(sMain, Sensor.ACTIVE, Main);

        OBlock East = _OBlockMgr.getByUserName("East");
        Sensor sEast = East.getSensor();
        Assert.assertNotNull("Senor sFarWest found", sEast);
        NXFrameTest.setAndConfirmSensorAction(sEast, Sensor.ACTIVE, East);

        tta.actionPerformed(null);  // called to make TrackerTableAction._frame
        Tracker TkrWest = new Tracker(West, "TkrFW", null, tta);
        Assert.assertNotNull("Tracker TkrWest found", TkrWest);

        JFrameOperator nfo = new JFrameOperator(tta._frame);
        JDialogOperator jdo = new JDialogOperator(nfo, Bundle.getMessage("TrackerTitle"));
        Assert.assertNotNull("Dialog operator found", jdo);
        
        Tracker.ChooseStartBlock dialog = (Tracker.ChooseStartBlock)jdo.getSource();
        Assert.assertNotNull("JDialog found", dialog);
        
        dialog._jList.setSelectedIndex(0);

        List<OBlock> occupied = TkrWest.getBlocksOccupied();
        Assert.assertEquals("TkrWest Blocks Occupied", 2, occupied.size());
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);
        
        dialog._jList.setSelectedIndex(0);

        occupied = TkrWest.getBlocksOccupied();
        Assert.assertEquals("TkrWest Blocks Occupied", 3, occupied.size());

        ControlPanelEditor panel = (ControlPanelEditor) jmri.util.JmriJFrame.getFrame("Indicator Demo 0 Editor");
        panel.dispose();
        _OBlockMgr.dispose();
    }        

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initOBlockManager();
        JUnitUtil.initDebugThrottleManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.clearShutDownManager();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(TrackerTest.class);

}
