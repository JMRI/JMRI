package jmri.jmrit.logix;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;

import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests that a Wait Sensor command in a warrant script only ends when
 * the sensor reaches the requested state, the operator releases the train,
 * or the warrant run ends.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class EngineerSensorWaitTest {

    private Warrant warrant;
    private Sensor waitSensor;

    private Engineer getEngineer() {
        try {
            Field f = Warrant.class.getDeclaredField("_engineer");
            f.setAccessible(true);
            return (Engineer) f.get(warrant);
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    @Test
    public void testSensorWaitIgnoresOtherWakeUps() {
        String msg = warrant.setRunMode(Warrant.MODE_RUN, null, null, null, false);
        assertNull(msg, "setRunMode - " + msg);
        JUnitUtil.waitFor(() -> {
            Engineer e = getEngineer();
            return e != null && e.getRunState() == Warrant.WAIT_FOR_SENSOR;
        }, "Engineer waits for sensor");
        Engineer engineer = getEngineer();
        assertNotNull(engineer);
        assertEquals(1, engineer.getCurrentCommandIndex(), "at Wait Sensor command");

        // the end of a speed ramp calls setHalt(false), which wakes the Engineer;
        // the sensor is still inactive
        engineer.setHalt(false);
        JUnitUtil.waitFor(200);
        assertEquals(Warrant.WAIT_FOR_SENSOR, engineer.getRunState(), "still waiting for the sensor");
        assertEquals(1, engineer.getCurrentCommandIndex(), "script held at Wait Sensor command");
        assertEquals(0.0f, engineer.getSpeedSetting(), 0.001f, "train has not started");

        // a change to the wrong state does not end the wait either
        ThreadingUtil.runOnLayout(() -> assertDoesNotThrow(() -> waitSensor.setState(Sensor.UNKNOWN)));
        JUnitUtil.waitFor(200);
        assertEquals(Warrant.WAIT_FOR_SENSOR, engineer.getRunState(), "still waiting after other state");

        // the sensor goes active and the script continues
        ThreadingUtil.runOnLayout(() -> assertDoesNotThrow(() -> waitSensor.setState(Sensor.ACTIVE)));
        JUnitUtil.waitFor(() -> engineer.getCurrentCommandIndex() >= 2, "script continues after sensor");
        assertNotEquals(Warrant.WAIT_FOR_SENSOR, engineer.getRunState(), "no longer waiting for the sensor");
    }

    @Test
    public void testResumeReleasesSensorWait() {
        String msg = warrant.setRunMode(Warrant.MODE_RUN, null, null, null, false);
        assertNull(msg, "setRunMode - " + msg);
        JUnitUtil.waitFor(() -> {
            Engineer e = getEngineer();
            return e != null && e.getRunState() == Warrant.WAIT_FOR_SENSOR;
        }, "Engineer waits for sensor");
        Engineer engineer = getEngineer();
        assertNotNull(engineer);
        assertEquals(1, engineer.getCurrentCommandIndex(), "at Wait Sensor command");

        // the operator presses Resume, for example because the sensor has failed
        assertTrue(warrant.controlRunTrain(Warrant.RESUME), "Resume accepted");
        JUnitUtil.waitFor(() -> engineer.getCurrentCommandIndex() >= 2, "script continues after Resume");
        assertNotEquals(Warrant.WAIT_FOR_SENSOR, engineer.getRunState(), "no longer waiting for the sensor");
        assertEquals(Sensor.INACTIVE, waitSensor.getKnownState(), "sensor did not change");
        assertFalse(Arrays.asList(waitSensor.getPropertyChangeListeners()).contains(engineer),
                "Engineer no longer listens to the sensor");
    }

    @Test
    public void testStopEndsSensorWait() {
        String msg = warrant.setRunMode(Warrant.MODE_RUN, null, null, null, false);
        assertNull(msg, "setRunMode - " + msg);
        JUnitUtil.waitFor(() -> {
            Engineer e = getEngineer();
            return e != null && e.getRunState() == Warrant.WAIT_FOR_SENSOR;
        }, "Engineer waits for sensor");
        Engineer engineer = getEngineer();
        assertNotNull(engineer);

        // a stop that is not an abort also ends the wait, without an error
        warrant.stopWarrant(false, false);
        JUnitUtil.waitFor(() -> !engineer.isAlive(), "Engineer ended");
        assertEquals(Warrant.MODE_NONE, warrant.getRunMode(), "warrant stopped");
        assertFalse(Arrays.asList(waitSensor.getPropertyChangeListeners()).contains(engineer),
                "Engineer no longer listens to the sensor");
    }

    @Test
    public void testSensorAlreadyInStateDoesNotWait() {
        ThreadingUtil.runOnLayout(() -> assertDoesNotThrow(() -> waitSensor.setState(Sensor.ACTIVE)));
        String msg = warrant.setRunMode(Warrant.MODE_RUN, null, null, null, false);
        assertNull(msg, "setRunMode - " + msg);
        JUnitUtil.waitFor(() -> {
            Engineer e = getEngineer();
            return e != null && e.getCurrentCommandIndex() >= 2;
        }, "script passes Wait Sensor command");
        assertFalse(Arrays.asList(waitSensor.getPropertyChangeListeners()).contains(getEngineer()),
                "Engineer no longer listens to the sensor");
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
        waitSensor = sensorMgr.newSensor("IS9", "PlatformSensor");
        bA.setSensor("IS1");
        bB.setSensor("IS2");
        bC.setSensor("IS3");
        assertDoesNotThrow(() -> sA.setState(Sensor.ACTIVE));
        assertDoesNotThrow(() -> waitSensor.setState(Sensor.INACTIVE));

        warrant = new Warrant("IW1", "SensorWaitWarrant");
        ArrayList<BlockOrder> orders = new ArrayList<>();
        orders.add(new BlockOrder(bA, "AtoB", null, "AB"));
        orders.add(new BlockOrder(bB, "BtoC", "AB", "BC"));
        orders.add(new BlockOrder(bC, "CfromB", "BC", null));
        warrant.setBlockOrders(orders);

        warrant.setThrottleCommands(new ArrayList<>());
        warrant.addThrottleCommand(new ThrottleSetting(0, "Speed", "0.0", "A"));
        warrant.addThrottleCommand(new ThrottleSetting(0, "Wait Sensor", "Active", "IS9"));
        // long wait keeps the train standing after the sensor wait
        warrant.addThrottleCommand(new ThrottleSetting(60000, "Speed", "0.4", "A"));
        warrant.addThrottleCommand(new ThrottleSetting(100, "NoOp", "Enter Block", "B"));
        warrant.addThrottleCommand(new ThrottleSetting(100, "NoOp", "Enter Block", "C"));
        warrant.addThrottleCommand(new ThrottleSetting(100, "Speed", "0.0", "C"));
        warrant.getSpeedUtil().setAddress("999(L)");
        warrant.setTrainName("SensorTrain");
        WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);
    }

    @AfterEach
    public void tearDown() {
        warrant.stopWarrant(true, true);
        warrant = null;
        waitSensor = null;
        JUnitUtil.tearDown();
    }

}
