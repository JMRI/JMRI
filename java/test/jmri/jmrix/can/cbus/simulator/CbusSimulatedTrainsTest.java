package jmri.jmrix.can.cbus.simulator;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import jmri.Block;
import jmri.DccLocoAddress;
import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Sensor;
import jmri.SpeedStepMode;
import jmri.jmrit.simulation.SimulatedSpeedSource.Command;
import jmri.jmrit.simulation.SimulationTestUtil;
import jmri.jmrit.simulation.SimulationTruth;
import jmri.jmrit.simulation.VirtualTrain;
import jmri.jmrix.ConnectionConfig;
import jmri.jmrix.ConnectionConfigManager;
import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.CbusConstants;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.*;

/**
 * Simulated trains on CAN Simulation connections, made as the connection
 * preferences make them: driven by JMRI's CBUS throttles and by a simulated
 * cab on the bus, and reporting on CBUS sensors, including two connections
 * on one layout.
 * <p>
 * The trains move in real time, so these tests assert the order of events
 * and the ground truth, never how long anything took. They set no truth
 * offset: they check the CBUS path and JMRI's code, not estimates.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class CbusSimulatedTrainsTest {

    private static final DccLocoAddress LOCO = SimulationTestUtil.address(3);

    /** Sensor changes in the order the sensors made them. */
    private final List<String> changes = new CopyOnWriteArrayList<>();
    private final List<Sensor> recorded = new ArrayList<>();
    private final PropertyChangeListener recorder = e -> {
        if (Sensor.PROPERTY_KNOWN_STATE.equals(e.getPropertyName())) {
            Sensor s = (Sensor) e.getSource();
            int st = s.getKnownState();
            if (st == Sensor.ACTIVE || st == Sensor.INACTIVE) {
                changes.add(s.getUserName() + (st == Sensor.ACTIVE ? " active" : " inactive"));
            }
        }
    };

    /**
     * Blocks in a line, each with a CBUS sensor on the connection given for
     * it, user named after the Block.
     */
    private Block[] line(String[] names, CanSystemConnectionMemo[] on, String[] addresses) throws JmriException {
        Block[] b = SimulationTestUtil.line(500, names);
        for (int i = 0; i < b.length; i++) {
            Sensor s = CbusSimulationTestUtil.sensor(on[i], addresses[i]);
            String name = names[i];
            ThreadingUtil.runOnLayoutWithJmriException(() -> {
                s.setUserName(name);
                s.setKnownState(Sensor.INACTIVE);
            });
            b[i].setSensor(s.getSystemName());
            s.addPropertyChangeListener(recorder);
            recorded.add(s);
        }
        return b;
    }

    private static void assertBefore(List<String> list, String first, String then) {
        int a = list.indexOf(first);
        int b = list.indexOf(then);
        Assertions.assertTrue(a >= 0, first + " in " + list);
        Assertions.assertTrue(b >= 0, then + " in " + list);
        Assertions.assertTrue(a < b, first + " before " + then + " in " + list);
    }

    private static void waitStopped(VirtualTrain t) {
        SimulationTestUtil.waitForTruth(tr -> {
            SimulationTruth.TrainTruth tt = tr.getTrain(t);
            return tt != null && tt.getSpeedMmPerSecond() == 0;
        }, t.getName() + " stopped");
    }

    @Test
    public void testNothingFollowedUntilATrainIsPlaced() throws JmriException {
        Block[] b = line(new String[]{"A", "B"}, new CanSystemConnectionMemo[]{memo, memo},
                new String[]{"+N1E1", "+N1E2"});
        CbusSimulatedSpeedSource source = memo.get(CbusSimulator.class).getSimulatedSpeedSource();
        Assertions.assertFalse(source.isFollowing(), "before a train is placed");
        VirtualTrain t = SimulationTestUtil.train("T", 3, 100, 400);
        SimulationTestUtil.place(t, b[0], null, 200);
        Assertions.assertTrue(source.isFollowing(), "once a train is placed");
        JUnitUtil.waitFor(() -> changes.contains("A active"), "A reported on CBUS");
        SimulationTestUtil.removeTrains();
        Assertions.assertFalse(source.isFollowing(), "once the last train is removed");
        JUnitUtil.waitFor(() -> changes.contains("A inactive"), "A clear once the train is removed");
    }

    @Test
    public void testJmriThrottleDrivesTrainPastCbusSensors() throws JmriException {
        Block[] b = line(new String[]{"A", "B", "C", "D"}, new CanSystemConnectionMemo[]{memo, memo, memo, memo},
                new String[]{"+N1E1", "+N1E2", "-N1E3", "+4"});
        VirtualTrain t = SimulationTestUtil.train("T", 3, 100, 400);
        SimulationTestUtil.place(t, b[1], b[0], 150);
        JUnitUtil.waitFor(() -> changes.contains("B active"), "B reported on CBUS");

        DccThrottle th = CbusSimulationTestUtil.throttle(memo, LOCO);
        ThreadingUtil.runOnLayout(() -> {
            th.setIsForward(true);
            th.setSpeedSetting(0.5f);
        });
        SimulationTestUtil.waitForFrontIn(t, b[3]);
        ThreadingUtil.runOnLayout(() -> th.setSpeedSetting(0f));
        waitStopped(t);
        JUnitUtil.waitFor(() -> changes.contains("D active"), "D reported");

        assertBefore(changes, "B active", "C active");
        assertBefore(changes, "C active", "B inactive");
        assertBefore(changes, "B inactive", "D active");
        Assertions.assertFalse(changes.contains("A active"), "A never entered: " + changes);
        Assertions.assertEquals(b[3], SimulationTestUtil.truth(t).getFrontBlock());
        ThreadingUtil.runOnLayout(() -> th.release(null));
    }

    @Test
    public void testSimulatedCabDrivesTrainAndJmriSeesItsCommands() throws JmriException {
        Block[] b = line(new String[]{"A", "B", "C"}, new CanSystemConnectionMemo[]{memo, memo, memo},
                new String[]{"+N1E1", "+N1E2", "+N1E3"});
        VirtualTrain t = SimulationTestUtil.train("T", 3, 100, 400);
        SimulationTestUtil.place(t, b[0], null, 150);

        List<int[]> fromBus = new CopyOnWriteArrayList<>();
        CanListener l = new CanListener() {
            @Override
            public void message(CanMessage m) {
                // JMRI's own frames
            }

            @Override
            public void reply(CanReply r) {
                if (r.getElement(0) == CbusConstants.CBUS_DSPD) {
                    fromBus.add(new int[]{r.getElement(1), r.getElement(2)});
                }
            }
        };
        memo.getTrafficController().addCanListener(l);
        CbusSimulatedCab[] cab = new CbusSimulatedCab[1];
        ThreadingUtil.runOnLayout(() -> {
            cab[0] = new CbusSimulatedCab(memo);
            cab[0].acquire(LOCO);
        });
        JUnitUtil.waitFor(() -> cab[0].getSession() >= 0, "the cab's session");
        int session = cab[0].getSession();
        ThreadingUtil.runOnLayout(() -> cab[0].setSpeedDirection(0x80 | 64));
        SimulationTestUtil.waitForFrontIn(t, b[2]);
        ThreadingUtil.runOnLayout(() -> cab[0].setSpeedDirection(0x80));
        waitStopped(t);

        assertBefore(changes, "A active", "B active");
        assertBefore(changes, "B active", "C active");
        JUnitUtil.waitFor(() -> fromBus.size() >= 2, "JMRI saw the cab's commands");
        Assertions.assertArrayEquals(new int[]{session, 0x80 | 64}, fromBus.get(0), "as from another cab on the bus");
        Assertions.assertArrayEquals(new int[]{session, 0x80}, fromBus.get(1));

        ThreadingUtil.runOnLayout(() -> {
            cab[0].release();
            cab[0].dispose();
        });
        memo.getTrafficController().removeCanListener(l);
    }

    @Test
    public void testTwoConnectionsOnOneLayout() throws JmriException {
        CanSystemConnectionMemo memo2 = CbusSimulationTestUtil.addCanSimulation("M2");
        try {
            Block[] b = line(new String[]{"A", "B", "C", "D"},
                    new CanSystemConnectionMemo[]{memo, memo, memo2, memo2},
                    new String[]{"+N1E1", "+N1E2", "+N2E3", "+N2E4"});
            VirtualTrain t = SimulationTestUtil.train("T", 3, 100, 400);
            SimulationTestUtil.place(t, b[0], null, 150);
            Assertions.assertTrue(SimulationTestUtil.layout().getSensorsNotReported().isEmpty(),
                    "every sensor on one connection or the other is reported");
            DccThrottle th = CbusSimulationTestUtil.throttle(memo, LOCO);
            ThreadingUtil.runOnLayout(() -> {
                th.setIsForward(true);
                th.setSpeedSetting(0.5f);
            });
            SimulationTestUtil.waitForFrontIn(t, b[3]);
            ThreadingUtil.runOnLayout(() -> th.setSpeedSetting(0f));
            waitStopped(t);
            JUnitUtil.waitFor(() -> changes.contains("D active"), "D reported on the second connection");
            assertBefore(changes, "A active", "B active");
            assertBefore(changes, "B active", "C active");
            assertBefore(changes, "C active", "D active");
            ThreadingUtil.runOnLayout(() -> th.release(null));
            SimulationTestUtil.removeTrains();
        } finally {
            CbusSimulationTestUtil.dispose(memo2);
        }
    }

    /**
     * A connection that goes away while trains are placed: the simulated
     * trains stop using its command station and sensors, and carry on with the
     * other connection's.
     *
     * @throws JmriException if a sensor can't be set
     */
    @Test
    public void testConnectionRemovedIsNoLongerUsed() throws JmriException {
        CanSystemConnectionMemo memo2 = CbusSimulationTestUtil.addCanSimulation("M2");
        ConnectionConfig cc2 = null;
        for (ConnectionConfig c : InstanceManager.getDefault(ConnectionConfigManager.class).getConnections()) {
            if (c.getAdapter() != null && c.getAdapter().getSystemConnectionMemo() == memo2) {
                cc2 = c;
            }
        }
        Block[] b = line(new String[]{"A", "B", "C", "D"},
                new CanSystemConnectionMemo[]{memo, memo, memo2, memo2},
                new String[]{"+N1E1", "+N1E2", "+N2E3", "+N2E4"});
        Sensor c = b[2].getSensor();
        Sensor d = b[3].getSensor();
        VirtualTrain t = SimulationTestUtil.train("T", 3, 100, 400);
        SimulationTestUtil.place(t, b[0], null, 150);
        Assertions.assertTrue(SimulationTestUtil.layout().getSensorsNotReported().isEmpty(),
                "both connections' sensors are reported");

        CbusSimulationTestUtil.dispose(memo2);
        ConnectionConfig removed = Objects.requireNonNull(cc2, "the second connection is listed");
        ThreadingUtil.runOnLayout(() -> InstanceManager.getDefault(ConnectionConfigManager.class).remove(removed));
        Assertions.assertEquals(Set.of(c, d), SimulationTestUtil.layout().getSensorsNotReported(),
                "the removed connection's sensors are no longer reported");

        DccThrottle th = CbusSimulationTestUtil.throttle(memo, LOCO);
        ThreadingUtil.runOnLayout(() -> {
            th.setIsForward(true);
            th.setSpeedSetting(0.5f);
        });
        SimulationTestUtil.waitForFrontIn(t, b[2]);
        ThreadingUtil.runOnLayout(() -> th.setSpeedSetting(0f));
        waitStopped(t);
        assertBefore(changes, "A active", "B active");
        Assertions.assertFalse(changes.contains("C active"), "nothing sent on the removed connection: " + changes);
        ThreadingUtil.runOnLayout(() -> th.release(null));
    }

    /**
     * JMRI's CBUS throttle in each speed step mode, against the simulated
     * decoder: the step the decoder runs at is the throttle setting times the
     * mode's steps, rounded, and at least step 1 for any setting above 0.
     *
     * @throws JmriException if a sensor can't be set
     */
    @Test
    public void testThrottleSettingsReachTheDecoderInEachMode() throws JmriException {
        Block[] b = line(new String[]{"A", "B"}, new CanSystemConnectionMemo[]{memo, memo},
                new String[]{"+N1E1", "+N1E2"});
        VirtualTrain t = SimulationTestUtil.train("T", 3, 100, 400);
        SimulationTestUtil.place(t, b[0], null, 150);
        CbusSimulatedSpeedSource source = memo.get(CbusSimulator.class).getSimulatedSpeedSource();
        DccThrottle th = CbusSimulationTestUtil.throttle(memo, LOCO);
        float[] settings = {0.004f, 0.01f, 0.25f, 0.5f, 0.77f, 1.0f, 0f};
        for (SpeedStepMode mode : new SpeedStepMode[]{SpeedStepMode.NMRA_DCC_28, SpeedStepMode.NMRA_DCC_14,
                SpeedStepMode.NMRA_DCC_128}) {
            ThreadingUtil.runOnLayout(() -> th.setSpeedStepMode(mode));
            for (float s : settings) {
                int expected = s == 0 ? 0 : Math.max(1, Math.round(s * mode.numSteps));
                ThreadingUtil.runOnLayout(() -> th.setSpeedSetting(s));
                JUnitUtil.waitFor(() -> {
                    Command c = source.getCommand(LOCO);
                    return c != null && c.getMode() == mode && c.getSpeedStep() == expected;
                }, mode + " setting " + s + ": step " + expected + ", decoder has " + source.getCommand(LOCO));
            }
        }
        ThreadingUtil.runOnLayout(() -> th.setSpeedSetting(-1f));
        JUnitUtil.waitFor(() -> {
            Command c = source.getCommand(LOCO);
            return c != null && c.isEmergencyStop();
        }, "emergency stop");
        ThreadingUtil.runOnLayout(() -> th.release(null));
    }

    private CanSystemConnectionMemo memo;

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        SimulationTestUtil.setUpSimulatedProfile();
        memo = CbusSimulationTestUtil.addCanSimulation("M");
    }

    @AfterEach
    public void tearDown() {
        SimulationTestUtil.removeTrains();
        for (Sensor s : recorded) {
            s.removePropertyChangeListener(recorder);
        }
        CbusSimulationTestUtil.dispose(memo);
        memo = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
