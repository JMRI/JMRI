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
import org.netbeans.jemmy.operators.JFrameOperator;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class PortalManagerTest {

    private PortalManager _portalMgr;

    @Test
    public void testCTor() {
        Assert.assertNotNull("exists",_portalMgr);
    }

    @Test
    public void testCreatandGetPortal() {
        // the is was originally part of Warrant test, but none of the asserts
        // are testing anything in the warrant.
        OBlockManager _OBlockMgr = InstanceManager.getDefault(OBlockManager.class);
        OBlock bWest = _OBlockMgr.createNewOBlock("OB1", "West");
        OBlock bNorth = _OBlockMgr.createNewOBlock("OB3", "North");
        OBlock bSouth = _OBlockMgr.createNewOBlock("OB4", "South");
        
        Portal pNorthWest = _portalMgr.createNewPortal("NorthWest");
        pNorthWest.setToBlock(bWest, false);
        pNorthWest.setFromBlock(bNorth, false);
        Portal pSouthWest = _portalMgr.createNewPortal("SouthWest");
        pSouthWest.setToBlock(bWest, false);
        pSouthWest.setFromBlock(bSouth, false);        
        Assert.assertEquals("Portal", pNorthWest, _portalMgr.getPortal("NorthWest"));
        Assert.assertEquals("Portal Block", bSouth, _portalMgr.getPortal("SouthWest").getFromBlock());
        Assert.assertEquals("Portal", pSouthWest, bSouth.getPortalByName("SouthWest"));        
        Assert.assertEquals("Portal Block", "West", _portalMgr.getPortal("NorthWest").getToBlockName());
        Assert.assertEquals("Portal Block", "North", _portalMgr.getPortal("NorthWest").getFromBlockName());

        Portal pNorthEast = _portalMgr.createNewPortal("NorthEast");
        pNorthEast.setToBlock(_OBlockMgr.getOBlock("OB2"), false);
        pNorthEast.setFromBlock(_OBlockMgr.getOBlock("North"), false);
        Portal pSouthEast = _portalMgr.createNewPortal("SouthEast");
        OBlock east = _OBlockMgr.getOBlock("OB2");
        pSouthEast.setToBlock(east, false);
        pSouthEast.setFromBlock(_OBlockMgr.getOBlock("South"), false);
        
        Assert.assertEquals("Portal Block", east, _portalMgr.getPortal("SouthEast").getToBlock());
        Assert.assertEquals("Portal Block", "West", _portalMgr.getPortal("NorthWest").getToBlockName());
        Assert.assertEquals("Portal Block", _OBlockMgr.getOBlock("South"), _portalMgr.getPortal("SouthWest").getFromBlock());
    }

    @Test
    public void testChangeNames() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // load and display
        File f = new File("java/test/jmri/jmrit/logix/valid/ShortBlocksTest.xml");
        InstanceManager.getDefault(ConfigureManager.class).load(f);
        
        ControlPanelEditor panel = (ControlPanelEditor) jmri.util.JmriJFrame.getFrame("LinkedWarrantsTest");

        WarrantTableFrame tableFrame = WarrantTableFrame.getDefault();
        Assert.assertNotNull("tableFrame", tableFrame);

        Warrant warrant = InstanceManager.getDefault(WarrantManager.class).getWarrant("WestToEast");
        Assert.assertNotNull("warrant", warrant);
//        List<BlockOrder> orders = warrant.getBlockOrders();
        BlockOrder order =  warrant.getBlockOrders().get(3);
        OBlock blockOB6 = order.getBlock();
        Portal portal = _portalMgr.getPortal("MidWestToMiddle");
        OPath path = order.getPath();
        // names as loaded
        Assert.assertEquals("Block Name", "Middle", blockOB6.getUserName());
        Assert.assertEquals("Entry Portal Name", "MidWestToMiddle", order.getEntryName());
        Assert.assertEquals("Path Name", "MainMidShort", order.getPathName());
        Assert.assertEquals("Path Name from path", "MainMidShort", path.getName());

        // change names
        blockOB6.setUserName("AnotherBlock");
        portal.setName("AnotherPortal");
        Assert.assertEquals("Block Name", "AnotherBlock", blockOB6.getUserName());
        Assert.assertEquals("Entry Portal Name", "AnotherPortal", order.getEntryName());
        path.setName("AnotherPath");
        Assert.assertEquals("Path Name", "AnotherPath", order.getPathName());
        Assert.assertEquals("Path Name from path", "AnotherPath", path.getName());

        // Run the warrant to prove name changes hold
        OBlockManager _OBlockMgr = InstanceManager.getDefault(OBlockManager.class);
        Sensor sensor1 = InstanceManager.getDefault(SensorManager.class).getBySystemName("IS1");
        NXFrameTest.setAndConfirmSensorAction(sensor1, Sensor.ACTIVE, _OBlockMgr.getBySystemName("OB1"));
        // WarrantTable.runTrain() returns a string that is not null if the 
        // warrant can't be started 
        Assert.assertNull("Warrant starts",
              tableFrame.runTrain(warrant, Warrant.MODE_RUN)); // start run

        Warrant w = warrant;
        jmri.util.JUnitUtil.waitFor(() -> {
            String m =  w.getRunningMessage();
            return m.endsWith("Cmd #8.");
        }, "Train starts to move at 8th command");

       // OBlock of route
        String[] route1 = {"OB1", "OB3", "OB5", "OB6", "OB7", "OB9", "OB11"};
        OBlock block = _OBlockMgr.getOBlock("OB11");

        // Run the train, then checks end location
        Assert.assertEquals("Fred made it to block OB11", 
                block.getSensor().getDisplayName(), 
                NXFrameTest.runtimes(route1, _OBlockMgr).getDisplayName());

        // passed test - cleanup.
        JFrameOperator jfo = new JFrameOperator(tableFrame);
        jfo.requestClose();
        panel.dispose();    // disposing this way allows test to be rerun (i.e. reload panel file) multiple times
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initDebugPowerManager();
        JUnitUtil.initOBlockManager();
        JUnitUtil.initWarrantManager();
        JUnitUtil.initDebugThrottleManager();

        _portalMgr = InstanceManager.getDefault(PortalManager.class);        
    }

    @After
    public void tearDown() {
        _portalMgr = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(PortalManagerTest.class);

}
