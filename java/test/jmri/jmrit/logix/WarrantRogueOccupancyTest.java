package jmri.jmrit.logix;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests that occupancy of a block beyond a dark block and a detected
 * block does not move the position of a warrant's train.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class WarrantRogueOccupancyTest {

    private Warrant warrant;
    private OBlock bA;
    private OBlock bD;
    private Sensor sC;
    private Sensor sD;

    @Test
    public void testOccupancyBeyondDetectedBlockIsRogue() {
        bA.setValue(warrant.getTrainName());
        String msg = warrant.allocateRoute(false, null);
        assertNull(msg, "allocateRoute - " + msg);
        msg = warrant.setRunMode(Warrant.MODE_RUN, null, null, null, false);
        assertNull(msg, "setRunMode - " + msg);
        JUnitUtil.waitFor(() -> warrant.getRunningMessage().contains("Cmd #2"), "Engineer waits at Cmd #2");
        assertEquals(0, warrant.getCurrentOrderIndex(), "train starts in block A");
        assertEquals(warrant, bD.getWarrant(), "block D is allocated to the warrant");

        // something occupies block D while the train is still in A.
        // B is dark, but C is detected and unoccupied, so this is not our train.
        ThreadingUtil.runOnLayout(() -> assertDoesNotThrow(() -> sD.setState(Sensor.ACTIVE)));
        JUnitUtil.waitFor(() -> bD.isOccupied(), "block D occupied");
        assertEquals(0, warrant.getCurrentOrderIndex(), "train position not moved by rogue occupancy");

        // the train passes the dark block B and enters C
        ThreadingUtil.runOnLayout(() -> assertDoesNotThrow(() -> sC.setState(Sensor.ACTIVE)));
        JUnitUtil.waitFor(() -> warrant.getCurrentOrderIndex() == 2, "train found in block C");
        // D is still occupied, so the warrant holds the train in C until D clears
        JUnitUtil.waitFor(() -> bD.equals(warrant._stoppingBlock), "warrant stops for occupied block D");
        assertEquals(2, warrant.getCurrentOrderIndex(), "train still in block C");
    }

    @BeforeEach
    public void setUp(@TempDir File tempDir) throws IOException {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager(new jmri.profile.NullProfile(tempDir));
        JUnitUtil.initDebugThrottleManager();
        JUnitUtil.initRosterConfigManager();

        OBlockManager blockMgr = InstanceManager.getDefault(OBlockManager.class);
        bA = blockMgr.createNewOBlock("OB1", "A");
        OBlock bB = blockMgr.createNewOBlock("OB2", "B");
        OBlock bC = blockMgr.createNewOBlock("OB3", "C");
        bD = blockMgr.createNewOBlock("OB4", "D");
        assertNotNull(bA);
        assertNotNull(bB);
        assertNotNull(bC);
        assertNotNull(bD);

        PortalManager portalMgr = InstanceManager.getDefault(PortalManager.class);
        Portal pAB = portalMgr.createNewPortal("AB");
        pAB.setFromBlock(bA, false);
        pAB.setToBlock(bB, false);
        Portal pBC = portalMgr.createNewPortal("BC");
        pBC.setFromBlock(bB, false);
        pBC.setToBlock(bC, false);
        Portal pCD = portalMgr.createNewPortal("CD");
        pCD.setFromBlock(bC, false);
        pCD.setToBlock(bD, false);

        bA.addPath(new OPath("AtoB", bA, null, pAB, new ArrayList<>()));
        bB.addPath(new OPath("BtoC", bB, pAB, pBC, new ArrayList<>()));
        bC.addPath(new OPath("CtoD", bC, pBC, pCD, new ArrayList<>()));
        bD.addPath(new OPath("DfromC", bD, pCD, null, new ArrayList<>()));

        // block B has no sensor, it is dark
        SensorManager sensorMgr = InstanceManager.getDefault(SensorManager.class);
        Sensor sA = sensorMgr.newSensor("IS1", "ASensor");
        sC = sensorMgr.newSensor("IS3", "CSensor");
        sD = sensorMgr.newSensor("IS4", "DSensor");
        bA.setSensor("IS1");
        bC.setSensor("IS3");
        bD.setSensor("IS4");
        assertDoesNotThrow(() -> sA.setState(Sensor.ACTIVE));
        assertDoesNotThrow(() -> sC.setState(Sensor.INACTIVE));
        assertDoesNotThrow(() -> sD.setState(Sensor.INACTIVE));
        assertTrue(bB.isDark(), "block B is dark");

        warrant = new Warrant("IW1", "RogueWarrant");
        ArrayList<BlockOrder> orders = new ArrayList<>();
        orders.add(new BlockOrder(bA, "AtoB", null, "AB"));
        orders.add(new BlockOrder(bB, "BtoC", "AB", "BC"));
        orders.add(new BlockOrder(bC, "CtoD", "BC", "CD"));
        orders.add(new BlockOrder(bD, "DfromC", "CD", null));
        for (int i = 0; i < orders.size(); i++) {
            orders.get(i).setIndex(i);
        }
        warrant.setBlockOrders(orders);
        warrant.setThrottleCommands(new ArrayList<>());
        warrant.addThrottleCommand(new ThrottleSetting(0, "Speed", "0.0", "A"));
        // long wait keeps the train standing in A during the test
        warrant.addThrottleCommand(new ThrottleSetting(60000, "Speed", "0.4", "A"));
        warrant.addThrottleCommand(new ThrottleSetting(100, "NoOp", "Enter Block", "B"));
        warrant.addThrottleCommand(new ThrottleSetting(100, "NoOp", "Enter Block", "C"));
        warrant.addThrottleCommand(new ThrottleSetting(100, "NoOp", "Enter Block", "D"));
        warrant.addThrottleCommand(new ThrottleSetting(100, "Speed", "0.0", "D"));
        warrant.getSpeedUtil().setAddress("999(L)");
        warrant.setTrainName("RogueTest");
        WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);
    }

    @AfterEach
    public void tearDown() {
        warrant.stopWarrant(true, true);
        warrant = null;
        bA = null;
        bD = null;
        sC = null;
        sD = null;
        JUnitUtil.tearDown();
    }

}
