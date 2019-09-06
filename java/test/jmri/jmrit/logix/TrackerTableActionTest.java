package jmri.jmrit.logix;

import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SensorManager;
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

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class TrackerTableActionTest {

    OBlockManager _OBlockMgr;
    SensorManager _sensorMgr;

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
        // load and display
        File f = new File("java/test/jmri/jmrit/logix/valid/IndicatorDemoTest.xml");
        InstanceManager.getDefault(ConfigureManager.class).load(f);
        TrackerTableAction tta = jmri.InstanceManager.getDefault(TrackerTableAction.class);
        Assert.assertNotNull("TrackerTableAction not found", tta);
        OBlock Middle = _OBlockMgr.getByUserName("Middle");
        Sensor sMiddle = _sensorMgr.getSensor("IS24");
        Assert.assertNotNull("Senor sMiddle not found", sMiddle);
        sMiddle.setState(Sensor.ACTIVE);
        new org.netbeans.jemmy.QueueTool().waitEmpty(200);  //pause for block to get occupied

        tta.markNewTracker(Middle, "Tkr1", null);
        Tracker Tkr1 = tta.findTrackerIn(Middle);
        Assert.assertNotNull("Tracker Tkr1 not found", tta);
        List<OBlock> occupied = Tkr1.getBlocksOccupied();
        Assert.assertEquals("Tkr1 Blocks Occupied", 1, occupied.size());
        
        Sensor sFarEast = _sensorMgr.getByUserName("FarEast");
        Assert.assertNotNull("Senor FarEast not found", sFarEast);
        sFarEast.setState(Sensor.ACTIVE);
        occupied = Tkr1.getBlocksOccupied();
        Assert.assertEquals("Tkr1 2 Blocks Occupied", 2, occupied.size());
        
        Sensor sEast = _sensorMgr.getByUserName("block3");
        Assert.assertNotNull("Senor East not found", sEast);
        sEast.setState(Sensor.ACTIVE);
        occupied = Tkr1.getBlocksOccupied();
        Assert.assertEquals("Tkr1 2 Blocks Occupied", 3, occupied.size());
        
        sMiddle.setState(Sensor.INACTIVE);
        occupied = Tkr1.getBlocksOccupied();
        Assert.assertEquals("Tkr1 2 Blocks Occupied", 2, occupied.size());

        
        sFarEast.setState(Sensor.INACTIVE);
        occupied = Tkr1.getBlocksOccupied();
        Assert.assertEquals("Tkr1 1 Blocks Occupied", 1, occupied.size());

        OBlock East = _OBlockMgr.getByUserName("East");
        tta.stopTracker(Tkr1, East);
    }

    @Test
    public void testTrackingDark() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // load and display
        File f = new File("java/test/jmri/jmrit/logix/valid/IndicatorDemoTest.xml");
        InstanceManager.getDefault(ConfigureManager.class).load(f);
        TrackerTableAction tta = jmri.InstanceManager.getDefault(TrackerTableAction.class);
        Assert.assertNotNull("TrackerTableAction not found", tta);
        OBlock Main = _OBlockMgr.getByUserName("Main");
        Sensor sMain = _sensorMgr.getByUserName("block2");
        Assert.assertNotNull("Senor sMain not found", sMain);
        sMain.setState(Sensor.ACTIVE);
        tta.markNewTracker(Main, "Tkr1", null);
        Tracker TkrD = tta.findTrackerIn(Main);
        Assert.assertNotNull("Tracker TkrD not found", TkrD);
        List<OBlock> occupied = TkrD.getBlocksOccupied();
        Assert.assertEquals("TkrD Blocks Occupied", 1, occupied.size());

        // passes through dark block "West Yard"
        Sensor sFarWest = _sensorMgr.getByUserName("FarWest");
        Assert.assertNotNull("Senor sFarWest not found", sFarWest);
        sFarWest.setState(Sensor.ACTIVE);
        occupied = TkrD.getBlocksOccupied();
        Assert.assertEquals("TkrD 3 Blocks Occupied", 3, occupied.size());
        
        sFarWest.setState(Sensor.INACTIVE);
        occupied = TkrD.getBlocksOccupied();
        Assert.assertEquals("TkrD 1 Blocks Occupied", 1, occupied.size());

        OBlock FarWest = _OBlockMgr.getByUserName("FarWest");
        tta.stopTracker(TkrD, FarWest);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initOBlockManager();
        _OBlockMgr = InstanceManager.getDefault(OBlockManager.class);
        _sensorMgr = InstanceManager.getDefault(SensorManager.class);
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(TrackerTableActionTest.class);

}
