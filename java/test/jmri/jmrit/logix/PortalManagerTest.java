package jmri.jmrit.logix;

import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.jmrit.display.controlPanelEditor.ControlPanelEditor;
import jmri.util.JUnitUtil;

import java.io.File;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.netbeans.jemmy.operators.JFrameOperator;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Paul Bender Copyright (C) 2017
 */
public class PortalManagerTest {

    private PortalManager _portalMgr;

    @Test
    public void testCTor() {
        assertThat(_portalMgr).withFailMessage("exists").isNotNull();
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
        assertThat(_portalMgr.getPortal("NorthWest")).withFailMessage("Portal").isEqualTo(pNorthWest);
        assertThat(_portalMgr.getPortal("SouthWest").getFromBlock()).withFailMessage("Portal Block").isEqualTo(bSouth);
        assertThat(bSouth.getPortalByName("SouthWest")).withFailMessage("Portal").isEqualTo(pSouthWest);
        assertThat(_portalMgr.getPortal("NorthWest").getToBlockName()).withFailMessage("Portal Block").isEqualTo("West");
        assertThat(_portalMgr.getPortal("NorthWest").getFromBlockName()).withFailMessage("Portal Block").isEqualTo("North");

        Portal pNorthEast = _portalMgr.createNewPortal("NorthEast");
        pNorthEast.setToBlock(_OBlockMgr.getOBlock("OB2"), false);
        pNorthEast.setFromBlock(_OBlockMgr.getOBlock("North"), false);
        Portal pSouthEast = _portalMgr.createNewPortal("SouthEast");
        OBlock east = _OBlockMgr.getOBlock("OB2");
        pSouthEast.setToBlock(east, false);
        pSouthEast.setFromBlock(_OBlockMgr.getOBlock("South"), false);

        assertThat(_portalMgr.getPortal("SouthEast").getToBlock()).withFailMessage("Portal Block").isEqualTo(east);
        assertThat(_portalMgr.getPortal("NorthWest").getToBlockName()).withFailMessage("Portal Block").isEqualTo("West");
        assertThat(_portalMgr.getPortal("SouthWest").getFromBlock()).withFailMessage("Portal Block").isEqualTo(_OBlockMgr.getOBlock("South"));
    }

    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    @DisabledIfSystemProperty(named ="jmri.skipTestsRequiringSeparateRunning", matches ="true")
    public void testChangeNames() throws Exception {
        // load and display
        File f = new File("java/test/jmri/jmrit/logix/valid/ShortBlocksTest.xml");
        InstanceManager.getDefault(ConfigureManager.class).load(f);
        jmri.util.JUnitAppender.suppressErrorMessage("Portal elem = null");

        ControlPanelEditor panel = (ControlPanelEditor) jmri.util.JmriJFrame.getFrame("LinkedWarrantsTest");

        WarrantTableFrame tableFrame = WarrantTableFrame.getDefault();
        assertThat(tableFrame).withFailMessage("tableFrame").isNotNull();

        Warrant warrant = InstanceManager.getDefault(WarrantManager.class).getWarrant("WestToEast");
        assertThat(warrant).withFailMessage("warrant").isNotNull();
        BlockOrder order =  warrant.getBlockOrders().get(3);
        OBlock blockOB6 = order.getBlock();
        Portal portal = _portalMgr.getPortal("MidWestToMiddle");
        OPath path = order.getPath();
        // names as loaded
        assertThat(blockOB6.getUserName()).withFailMessage("Block Name").isEqualTo("Middle");
        assertThat(order.getEntryName()).withFailMessage("Entry Portal Name").isEqualTo("MidWestToMiddle");
        assertThat(order.getPathName()).withFailMessage("Path Name").isEqualTo("MainMidShort");
        assertThat(path.getName()).withFailMessage("Path Name from path").isEqualTo("MainMidShort");

        // change names
        blockOB6.setUserName("AnotherBlock");
        portal.setName("AnotherPortal");
        assertThat(blockOB6.getUserName()).withFailMessage("Block Name").isEqualTo("AnotherBlock");
        assertThat(order.getEntryName()).withFailMessage("Entry Portal Name").isEqualTo("AnotherPortal");
        path.setName("AnotherPath");
        assertThat(order.getPathName()).withFailMessage("Path Name").isEqualTo("AnotherPath");
        assertThat(path.getName()).withFailMessage("Path Name from path").isEqualTo("AnotherPath");

        // Run the warrant to prove name changes hold
        OBlockManager _OBlockMgr = InstanceManager.getDefault(OBlockManager.class);
        Sensor sensor1 = InstanceManager.getDefault(SensorManager.class).getBySystemName("IS1");
        NXFrameTest.setAndConfirmSensorAction(sensor1, Sensor.ACTIVE, _OBlockMgr.getBySystemName("OB1"));
        // WarrantTable.runTrain() returns a string that is not null if the
        // warrant can't be started
        assertThat(tableFrame.runTrain(warrant, Warrant.MODE_RUN)).withFailMessage("Warrant starts").isNull(); // start run

        jmri.util.JUnitUtil.waitFor(() -> {
            String m =  warrant.getRunningMessage();
            return m.endsWith("Cmd #8.");
        }, "Train starts to move at 8th command");

       // OBlock of route
        String[] route1 = {"OB1", "OB3", "OB5", "OB6", "OB7", "OB9", "OB11"};
        OBlock block = _OBlockMgr.getOBlock("OB11");

        // Run the train, then checks end location
        assertThat(NXFrameTest.runtimes(route1, _OBlockMgr).getDisplayName()).withFailMessage("Fred made it to block OB11").isEqualTo(block.getSensor().getDisplayName());

        // passed test - cleanup.
        JFrameOperator jfo = new JFrameOperator(tableFrame);
        jfo.requestClose();
        assert panel != null;
        panel.dispose();    // disposing this way allows test to be rerun (i.e. reload panel file) multiple times
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
        _portalMgr = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.deregisterEditorManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(PortalManagerTest.class);

}
