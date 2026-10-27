package jmri.jmrit.logix;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests that a delayed ramp which replaces an earlier one stays under
 * the control of the warrant.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class WarrantDelayRampTest {

    private Warrant warrant;

    private Thread getDelayCommand() throws ReflectiveOperationException {
        Field f = Warrant.class.getDeclaredField("_delayCommand");
        f.setAccessible(true);
        synchronized (warrant) {
            return (Thread) f.get(warrant);
        }
    }

    private void rampSpeedDelay(long waitTime, String speedType, int idxSpeedChange) throws ReflectiveOperationException {
        Method m = Warrant.class.getDeclaredMethod("rampSpeedDelay", long.class, String.class, float.class, int.class);
        m.setAccessible(true);
        m.invoke(warrant, waitTime, speedType, 1.0f, idxSpeedChange);
    }

    private void cancelDelayRamp() throws ReflectiveOperationException {
        Method m = Warrant.class.getDeclaredMethod("cancelDelayRamp");
        m.setAccessible(true);
        m.invoke(warrant);
    }

    private Thread getEngineer() throws ReflectiveOperationException {
        Field f = Warrant.class.getDeclaredField("_engineer");
        f.setAccessible(true);
        return (Thread) f.get(warrant);
    }

    @Test
    public void testReplacedDelayRampCanStillBeCancelled() throws Exception {
        String msg = warrant.setRunMode(Warrant.MODE_RUN, null, null, null, false);
        assertNull(msg, "setRunMode - " + msg);
        JUnitUtil.waitFor(() -> {
            try {
                Thread t = getEngineer();
                return t != null && t.isAlive();
            } catch (ReflectiveOperationException e) {
                return false;
            }
        }, "Engineer running");

        // a delayed ramp to Stop before block C
        rampSpeedDelay(10000, Warrant.Stop, 2);
        Thread first = getDelayCommand();
        assertNotNull(first, "first delayed ramp scheduled");

        // a different request replaces it
        rampSpeedDelay(10000, "Restricted", 2);
        Thread second = getDelayCommand();
        assertNotNull(second, "second delayed ramp scheduled");
        assertNotSame(first, second, "first delayed ramp replaced");

        // the cancelled thread ends promptly and must not drop the new one
        JUnitUtil.waitFor(() -> !first.isAlive(), "cancelled delayed ramp ends");
        assertSame(second, getDelayCommand(), "warrant still holds the replacing delayed ramp");

        // so the replacing ramp can still be cancelled
        cancelDelayRamp();
        JUnitUtil.waitFor(() -> !second.isAlive(), "replacing delayed ramp cancelled");
        assertNull(getDelayCommand(), "no delayed ramp after cancel");
    }

    @BeforeEach
    public void setUp(@TempDir File tempDir) throws IOException {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager(new jmri.profile.NullProfile(tempDir));
        JUnitUtil.initDebugThrottleManager();
        JUnitUtil.initRosterConfigManager();

        OBlockManager blockMgr = InstanceManager.getDefault(OBlockManager.class);
        OBlock bA = blockMgr.createNewOBlock("OB1", "A");
        OBlock bB = blockMgr.createNewOBlock("OB2", "B");
        OBlock bC = blockMgr.createNewOBlock("OB3", "C");
        assertNotNull(bA);
        assertNotNull(bB);
        assertNotNull(bC);

        PortalManager portalMgr = InstanceManager.getDefault(PortalManager.class);
        Portal pAB = portalMgr.createNewPortal("AB");
        pAB.setFromBlock(bA, false);
        pAB.setToBlock(bB, false);
        Portal pBC = portalMgr.createNewPortal("BC");
        pBC.setFromBlock(bB, false);
        pBC.setToBlock(bC, false);

        OPath path = new OPath("AtoB", bA, null, pAB, new ArrayList<>());
        bA.addPath(path);
        path = new OPath("BtoC", bB, pAB, pBC, new ArrayList<>());
        path.setLength(200);
        bB.addPath(path);
        path = new OPath("CfromB", bC, pBC, null, new ArrayList<>());
        bC.addPath(path);
        bC.setLength(100);

        SensorManager sensorMgr = InstanceManager.getDefault(SensorManager.class);
        Sensor sA = sensorMgr.newSensor("IS1", "ASensor");
        sensorMgr.newSensor("IS2", "BSensor");
        sensorMgr.newSensor("IS3", "CSensor");
        bA.setSensor("IS1");
        bB.setSensor("IS2");
        bC.setSensor("IS3");
        assertDoesNotThrow(() -> sA.setState(Sensor.ACTIVE));

        warrant = new Warrant("IW1", "DelayRampWarrant");
        ArrayList<BlockOrder> orders = new ArrayList<>();
        orders.add(new BlockOrder(bA, "AtoB", null, "AB"));
        orders.add(new BlockOrder(bB, "BtoC", "AB", "BC"));
        orders.add(new BlockOrder(bC, "CfromB", "BC", null));
        warrant.setBlockOrders(orders);

        warrant.setThrottleCommands(new ArrayList<>());
        warrant.addThrottleCommand(new ThrottleSetting(0, "Speed", "0.0", "A"));
        // long wait keeps the engineer idle during the test
        warrant.addThrottleCommand(new ThrottleSetting(60000, "Speed", "0.4", "A"));
        warrant.addThrottleCommand(new ThrottleSetting(100, "NoOp", "Enter Block", "B"));
        warrant.addThrottleCommand(new ThrottleSetting(100, "NoOp", "Enter Block", "C"));
        warrant.addThrottleCommand(new ThrottleSetting(100, "Speed", "0.0", "C"));
        warrant.getSpeedUtil().setAddress("999(L)");
        warrant.setTrainName("DelayTrain");
        WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);
    }

    @AfterEach
    public void tearDown() {
        warrant.stopWarrant(true, true);
        warrant = null;
        JUnitUtil.tearDown();
    }

}
