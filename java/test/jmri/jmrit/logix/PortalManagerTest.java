package jmri.jmrit.logix;

import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.jmrit.display.controlPanelEditor.ControlPanelEditor;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import java.io.File;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.netbeans.jemmy.operators.JFrameOperator;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Paul Bender Copyright (C) 2017
 */
public class PortalManagerTest {

    private PortalManager _portalMgr;

    @Test
    public void testCTor() {
        assertNotNull( _portalMgr, "exists");
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
        assertEquals( pNorthWest, _portalMgr.getPortal("NorthWest"), "Portal");
        assertEquals( bSouth, _portalMgr.getPortal("SouthWest").getFromBlock(), "Portal Block");
        assertNotNull(bSouth);
        assertEquals( pSouthWest, bSouth.getPortalByName("SouthWest"), "Portal");
        assertEquals( "West", _portalMgr.getPortal("NorthWest").getToBlockName(), "Portal Block");
        assertEquals( "North", _portalMgr.getPortal("NorthWest").getFromBlockName(), "Portal Block");

        Portal pNorthEast = _portalMgr.createNewPortal("NorthEast");
        pNorthEast.setToBlock(_OBlockMgr.getOBlock("OB2"), false);
        pNorthEast.setFromBlock(_OBlockMgr.getOBlock("North"), false);
        Portal pSouthEast = _portalMgr.createNewPortal("SouthEast");
        OBlock east = _OBlockMgr.getOBlock("OB2");
        pSouthEast.setToBlock(east, false);
        pSouthEast.setFromBlock(_OBlockMgr.getOBlock("South"), false);

        assertEquals( east, _portalMgr.getPortal("SouthEast").getToBlock(), "Portal Block");
        assertEquals( "West", _portalMgr.getPortal("NorthWest").getToBlockName(), "Portal Block");
        assertEquals( _OBlockMgr.getOBlock("South"), _portalMgr.getPortal("SouthWest").getFromBlock(), "Portal Block");
    }

    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    @DisabledIfSystemProperty(named ="jmri.skipTestsRequiringSeparateRunning", matches ="true")
    public void testChangeNames() throws Exception {
        // load and display
        File f = new File("java/test/jmri/jmrit/logix/valid/ShortBlocksTest.xml");
        InstanceManager.getDefault(ConfigureManager.class).load(f);
        JUnitAppender.suppressErrorMessage("Portal elem = null");

        ControlPanelEditor panel = (ControlPanelEditor) jmri.util.JmriJFrame.getFrame("LinkedWarrantsTest");

        WarrantTableFrame tableFrame = WarrantTableFrame.getDefault();
        assertNotNull( tableFrame, "tableFrame");

        Warrant warrant = InstanceManager.getDefault(WarrantManager.class).getWarrant("WestToEast");
        assertNotNull( warrant, "warrant");
        BlockOrder order =  warrant.getBlockOrders().get(3);
        OBlock blockOB6 = order.getBlock();
        Portal portal = _portalMgr.getPortal("MidWestToMiddle");
        OPath path = order.getPath();
        // names as loaded
        assertEquals( "Middle", blockOB6.getUserName(), "Block Name");
        assertEquals( "MidWestToMiddle", order.getEntryName(), "Entry Portal Name");
        assertEquals( "MainMidShort", order.getPathName(), "MainMidShort");
        assertEquals( "MainMidShort", path.getName(), "Path Name from path");

        // change names
        blockOB6.setUserName("AnotherBlock");
        portal.setName("AnotherPortal");
        assertEquals( "AnotherBlock", blockOB6.getUserName(), "Block Name");
        assertEquals( "AnotherPortal", order.getEntryName(), "Entry Portal Name");
        path.setName("AnotherPath");
        assertEquals( "AnotherPath", order.getPathName(), "Path Name");
        assertEquals( "AnotherPath", path.getName(), "Path Name from path");

        // Run the warrant to prove name changes hold
        OBlockManager _OBlockMgr = InstanceManager.getDefault(OBlockManager.class);
        Sensor sensor1 = InstanceManager.getDefault(SensorManager.class).getBySystemName("IS1");
        NXFrameTest.setAndConfirmSensorAction(sensor1, Sensor.ACTIVE, _OBlockMgr.getBySystemName("OB1"));
        // WarrantTable.runTrain() returns a string that is not null if the
        // warrant can't be started
        assertNull(tableFrame.runTrain(warrant, Warrant.MODE_RUN), "Warrant starts"); // start run

        JUnitUtil.waitFor(() -> {
            String m =  warrant.getRunningMessage();
            return m.endsWith("Cmd #8.");
        }, "Train starts to move at 8th command");

       // OBlock of route
        String[] route1 = {"OB1", "OB3", "OB5", "OB6", "OB7", "OB9", "OB11"};
        OBlock block = _OBlockMgr.getOBlock("OB11");

        // Run the train, then checks end location
        assertEquals( block.getSensor().getDisplayName(),
            NXFrameTest.runtimes(route1, _OBlockMgr).getDisplayName(),
            "Fred made it to block OB11");

        // passed test - cleanup.
        JFrameOperator jfo = new JFrameOperator(tableFrame);
        jfo.requestClose();
        jfo.waitClosed();
        assertNotNull(panel);

        // disposing this way allows test to be rerun (i.e. reload panel file) multiple times
        Boolean retVal = jmri.util.ThreadingUtil.runOnGUIwithReturn(() -> {
            panel.dispose();
            return true;
        });
        assertTrue(retVal);

        JUnitUtil.waitThreadTerminated("WestToEast Killer");

    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initDebugPowerManager();
        JUnitUtil.initOBlockManager();
        WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);
        JUnitUtil.initWarrantManager();
        JUnitUtil.initDebugThrottleManager();

        _portalMgr = InstanceManager.getDefault(PortalManager.class);
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.removeMatchingThreads("Engineer(");
        _portalMgr = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(PortalManagerTest.class);

}
