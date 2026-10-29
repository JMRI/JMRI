package jmri.jmrit.logix;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the wait time a warrant computes before a planned speed change
 * from the commands of its script.
 *
 * Layout: A - B - C, each path 2000 mm.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class WarrantScriptWaitTest {

    private Warrant warrant;

    private Object getField(Object obj, Class<?> cls, String name) throws ReflectiveOperationException {
        Field f = cls.getDeclaredField(name);
        f.setAccessible(true);
        return f.get(obj);
    }

    /**
     * Start the warrant and wait until its Engineer is waiting to run the
     * command at cmdIdx, then compute the wait before a stop at block C,
     * starting from that command.
     *
     * @return the computed wait in ms, or null if the slow-down was started at once
     */
    private Long scriptWait(int cmdIdx, float availDist) throws Exception {
        String msg = warrant.setRunMode(Warrant.MODE_RUN, null, null, null, false);
        assertNull(msg, "setRunMode - " + msg);
        JUnitUtil.waitFor(() -> {
            try {
                Engineer e = (Engineer) getField(warrant, Warrant.class, "_engineer");
                return e != null && e.getCurrentCommandIndex() >= cmdIdx;
            } catch (ReflectiveOperationException e) {
                return false;
            }
        }, "Engineer reaches command " + cmdIdx);

        Method m = Warrant.class.getDeclaredMethod("makeScriptWait",
                float.class, float.class, int.class, String.class, int.class);
        m.setAccessible(true);
        m.invoke(warrant, availDist, 100f, 2, Warrant.Stop, cmdIdx);

        Object delay;
        synchronized (warrant) {
            delay = getField(warrant, Warrant.class, "_delayCommand");
        }
        if (delay == null) {
            return null;
        }
        return (Long) getField(delay, delay.getClass(), "_waitTime");
    }

    @Test
    public void testScriptWaitAtSteadySpeed() throws Exception {
        warrant.addThrottleCommand(new ThrottleSetting(0, "Speed", "0.5", "A"));
        warrant.addThrottleCommand(new ThrottleSetting(4000, "F1", "true", "A"));
        warrant.addThrottleCommand(new ThrottleSetting(4000, "F2", "true", "A"));
        warrant.addThrottleCommand(new ThrottleSetting(4000, "F3", "true", "A"));
        warrant.addThrottleCommand(new ThrottleSetting(2000, "NoOp", "Enter Block", "B"));
        warrant.addThrottleCommand(new ThrottleSetting(2000, "NoOp", "Enter Block", "C"));
        warrant.addThrottleCommand(new ThrottleSetting(500, "Speed", "0.0", "C"));

        // Worked calculation: the train runs at a steady throttle 0.5, so it covers
        // trackSpeed(0.5) mm every ms. The train is in the first block with plenty of
        // room, so no distance is kept back for the ramp, and the ramp must start
        // once the train has covered the distance it would cover in 6000 ms.
        float trackSpeed = warrant.getSpeedUtil().getTrackSpeed(0.5f);   // mm/ms
        assertTrue(trackSpeed > 0, "track speed known");
        Long wait = scriptWait(1, trackSpeed * 6000);
        assertNotNull(wait, "speed change is delayed, not started at once");
        assertEquals(6000, wait, 60, "wait is the time to cover the distance at the steady speed");
    }

    @Test
    public void testScriptWaitWithStopInScript() throws Exception {
        warrant.addThrottleCommand(new ThrottleSetting(0, "Speed", "0.5", "A"));
        warrant.addThrottleCommand(new ThrottleSetting(200, "Speed", "0.0", "A"));   // station stop
        warrant.addThrottleCommand(new ThrottleSetting(600, "Speed", "0.5", "A"));
        warrant.addThrottleCommand(new ThrottleSetting(2000, "NoOp", "Enter Block", "B"));
        warrant.addThrottleCommand(new ThrottleSetting(2000, "NoOp", "Enter Block", "C"));
        warrant.addThrottleCommand(new ThrottleSetting(500, "Speed", "0.0", "C"));

        // the stop is at block C, far beyond what the script covers in block A
        Long wait = scriptWait(0, 100000f);
        JUnitAppender.assertWarnMessageStartsWith("ScriptWaitWarrant: script unfit cmd#4.");
        assertNotNull(wait, "speed change is delayed, not started at once");
        assertTrue(wait > 2800 - 1000, "wait includes the scripted times and the stop, was " + wait);
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
        path.setLength(2000);
        bA.addPath(path);
        path = new OPath("BtoC", bB, pAB, pBC, new ArrayList<>());
        path.setLength(2000);
        bB.addPath(path);
        path = new OPath("CfromB", bC, pBC, null, new ArrayList<>());
        path.setLength(2000);
        bC.addPath(path);

        SensorManager sensorMgr = InstanceManager.getDefault(SensorManager.class);
        Sensor sA = sensorMgr.newSensor("IS1", "ASensor");
        sensorMgr.newSensor("IS2", "BSensor");
        sensorMgr.newSensor("IS3", "CSensor");
        bA.setSensor("IS1");
        bB.setSensor("IS2");
        bC.setSensor("IS3");
        assertDoesNotThrow(() -> sA.setState(Sensor.ACTIVE));

        warrant = new Warrant("IW1", "ScriptWaitWarrant");
        ArrayList<BlockOrder> orders = new ArrayList<>();
        orders.add(new BlockOrder(bA, "AtoB", null, "AB"));
        orders.add(new BlockOrder(bB, "BtoC", "AB", "BC"));
        orders.add(new BlockOrder(bC, "CfromB", "BC", null));
        warrant.setBlockOrders(orders);

        warrant.setThrottleCommands(new ArrayList<>());
        warrant.getSpeedUtil().setAddress("999(L)");
        warrant.setTrainName("ScriptWaitTrain");
        WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);
    }

    @AfterEach
    public void tearDown() {
        warrant.stopWarrant(true, true);
        warrant = null;
        JUnitUtil.tearDown();
    }

}
