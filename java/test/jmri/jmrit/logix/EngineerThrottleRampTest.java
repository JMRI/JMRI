package jmri.jmrit.logix;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;

import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.LocoAddress;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.SystemConnectionMemo;
import jmri.ThrottleManager;
import jmri.jmrix.AbstractThrottle;
import jmri.jmrix.AbstractThrottleManager;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the speed ramp thread and the error handling of the Engineer
 * that runs a warrant script.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class EngineerThrottleRampTest {

    private Warrant warrant;
    private OBlock bA;
    private volatile boolean slowDownRampDone = false;

    private Engineer getEngineer() {
        try {
            Field f = Warrant.class.getDeclaredField("_engineer");
            f.setAccessible(true);
            return (Engineer) f.get(warrant);
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    private Engineer startWarrant(int minCommand) {
        String msg = warrant.setRunMode(Warrant.MODE_RUN, null, null, null, false);
        assertNull(msg, "setRunMode - " + msg);
        JUnitUtil.waitFor(() -> {
            Engineer e = getEngineer();
            return e != null && e.isAlive() && e.getCurrentCommandIndex() >= minCommand;
        }, "Engineer running");
        return getEngineer();
    }

    private void makeWarrant(String... commands) {
        warrant = new Warrant("IW1", "RampWarrant") {
            @Override
            protected void downRampDone(boolean stop, boolean halted, String speedType, int endBlockIdx) {
                if (slowDownRampDone) {
                    // end of a replaced ramp takes a while
                    JUnitUtil.waitFor(300);
                }
                super.downRampDone(stop, halted, speedType, endBlockIdx);
            }
        };
        ArrayList<BlockOrder> orders = new ArrayList<>();
        orders.add(new BlockOrder(bA, "AtoB", null, "AB"));
        orders.add(new BlockOrder(InstanceManager.getDefault(OBlockManager.class).getOBlock("B"), "BfromA", "AB", null));
        warrant.setBlockOrders(orders);
        warrant.setThrottleCommands(new ArrayList<>());
        for (int i = 0; i < commands.length; i += 3) {
            warrant.addThrottleCommand(new ThrottleSetting(Long.parseLong(commands[i]), commands[i + 1], commands[i + 2], "A"));
        }
        warrant.addThrottleCommand(new ThrottleSetting(60000, "NoOp", "Enter Block", "B"));
        warrant.addThrottleCommand(new ThrottleSetting(100, "Speed", "0.0", "B"));
        warrant.getSpeedUtil().setAddress("999(L)");
        warrant.setTrainName("RampTrain");
    }

    @Test
    public void testRampAtFirstCommand() {
        makeWarrant("60000", "Speed", "0.5");
        Engineer engineer = startWarrant(0);
        assertEquals(0, engineer.getCurrentCommandIndex(), "Engineer waits at first command");
        DccThrottle throttle = engineer._throttle;
        throttle.setSpeedSetting(0.5f);

        engineer.rampSpeedTo(Warrant.Stop, -1);
        JUnitUtil.waitFor(() -> throttle.getSpeedSetting() <= 0.0f, "train ramped down to stop");
        JUnitUtil.waitFor(() -> !engineer.isRamping(), "ramp finished");
    }

    @Test
    public void testReplacingRampIsNotLost() {
        makeWarrant("0", "Speed", "0.5", "60000", "Speed", "0.5");
        Engineer engineer = startWarrant(1);
        DccThrottle throttle = engineer._throttle;
        assertEquals(0.5f, throttle.getSpeedSetting(), 0.001f, "train at script speed");

        engineer.rampSpeedTo(Warrant.Stop, -1);
        JUnitUtil.waitFor(() -> throttle.getSpeedSetting() < 0.5f, "train slowing down");

        // replace the slow down by a ramp back to normal speed
        slowDownRampDone = true;
        engineer.rampSpeedTo(Warrant.Normal, -1);
        JUnitUtil.waitFor(() -> throttle.getSpeedSetting() >= 0.5f, "train back at script speed");
        JUnitUtil.waitFor(() -> !engineer.isRamping(), "ramp finished");
    }

    // a throttle with functions F0 to F68, as many systems have
    private static class Throttle69 extends AbstractThrottle {
        private final LocoAddress address;

        Throttle69(LocoAddress address, SystemConnectionMemo memo) {
            super(memo, 69);
            this.address = address;
        }

        @Override
        public LocoAddress getLocoAddress() {
            return address;
        }

        @Override
        protected void sendFunctionGroup1() {
        }

        @Override
        protected void sendFunctionGroup2() {
        }

        @Override
        protected void sendFunctionGroup3() {
        }

        @Override
        protected void sendFunctionGroup6() {
        }

        @Override
        protected void throttleDispose() {
        }
    }

    @Test
    public void testFunctionAbove28() {
        InternalSystemConnectionMemo memo = InstanceManager.getDefault(InternalSystemConnectionMemo.class);
        ThrottleManager tm = new AbstractThrottleManager(memo) {
            @Override
            public void requestThrottleSetup(LocoAddress a, boolean control) {
                notifyThrottleKnown(new Throttle69(a, adapterMemo), a);
            }

            @Override
            public boolean canBeLongAddress(int address) {
                return address >= 1;
            }

            @Override
            public boolean canBeShortAddress(int address) {
                return address <= 127;
            }

            @Override
            public boolean addressTypeUnique() {
                return false;
            }
        };
        memo.store(tm, ThrottleManager.class);
        InstanceManager.store(tm, ThrottleManager.class);

        makeWarrant("0", "Speed", "0.0", "0", "F29", "ON", "60000", "Speed", "0.5");
        Engineer engineer = startWarrant(2);
        assertTrue(engineer._throttle.getFunction(29), "F29 set by the script");
        assertEquals(Warrant.MODE_RUN, warrant.getRunMode(), "warrant still running");
    }

    @Test
    public void testEngineerErrorStopsTrain() {
        makeWarrant("0", "Speed", "0.5", "100", "F99", "ON", "60000", "Speed", "0.5");
        Engineer engineer = startWarrant(0);
        DccThrottle throttle = engineer._throttle;
        JUnitUtil.waitFor(() -> warrant.getRunMode() == Warrant.MODE_NONE, "warrant stopped after error");
        JUnitAppender.assertErrorMessageStartsWith("RampWarrant: Engineer stopped by error at Cmd #2");
        assertEquals(0.0f, throttle.getSpeedSetting(), 0.001f, "train stopped");
        JUnitUtil.waitFor(() -> !engineer.isAlive(), "Engineer ended");
    }

    @BeforeEach
    public void setUp(@TempDir File tempDir) throws IOException {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager(new jmri.profile.NullProfile(tempDir));
        JUnitUtil.initDebugThrottleManager();
        JUnitUtil.initRosterConfigManager();
        WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);
        WarrantPreferences.getDefault().setThrottleIncrement(0.1f);
        WarrantPreferences.getDefault().setTimeIncrement(500);

        OBlockManager blockMgr = InstanceManager.getDefault(OBlockManager.class);
        bA = blockMgr.createNewOBlock("OB1", "A");
        OBlock bB = blockMgr.createNewOBlock("OB2", "B");
        assertNotNull(bA);
        assertNotNull(bB);

        PortalManager portalMgr = InstanceManager.getDefault(PortalManager.class);
        Portal pAB = portalMgr.createNewPortal("AB");
        pAB.setFromBlock(bA, false);
        pAB.setToBlock(bB, false);

        OPath path = new OPath("AtoB", bA, null, pAB, new ArrayList<>());
        path.setLength(2000);
        bA.addPath(path);
        path = new OPath("BfromA", bB, pAB, null, new ArrayList<>());
        path.setLength(2000);
        bB.addPath(path);

        SensorManager sensorMgr = InstanceManager.getDefault(SensorManager.class);
        Sensor sA = sensorMgr.newSensor("IS1", "ASensor");
        sensorMgr.newSensor("IS2", "BSensor");
        bA.setSensor("IS1");
        bB.setSensor("IS2");
        assertDoesNotThrow(() -> sA.setState(Sensor.ACTIVE));
    }

    @AfterEach
    public void tearDown() {
        if (warrant != null) {
            warrant.stopWarrant(true, true);
        }
        warrant = null;
        bA = null;
        JUnitUtil.tearDown();
    }

}
