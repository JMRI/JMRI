package jmri.jmrix.dccpp.simulator.simulatedtrains;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

import jmri.Block;
import jmri.DccLocoAddress;
import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.LocoAddress;
import jmri.Sensor;
import jmri.ThrottleListener;
import jmri.jmrit.simulation.SimulationTestUtil;
import jmri.jmrit.simulation.SimulationTruth;
import jmri.jmrit.simulation.VirtualTrain;
import jmri.jmrix.ConnectionConfigManager;
import jmri.jmrix.dccpp.DCCppSensor;
import jmri.jmrix.dccpp.DCCppSystemConnectionMemo;
import jmri.jmrix.dccpp.simulator.ConnectionConfig;
import jmri.jmrix.dccpp.simulator.DCCppSimulatorAdapter;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.*;

/**
 * Simulated trains on a DCC-EX Simulator connection, made as the connection
 * preferences make it: driven by JMRI's DCC-EX throttle and reporting on
 * DCC-EX sensors through the simulated command station.
 * <p>
 * The trains move in real time, so these tests assert the order of events and
 * the ground truth, never how long anything took. The sensors are inputs 101
 * to 104, beyond the inputs the simulated command station sends random
 * replies for.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
@Timeout(60)
public class DCCppSimulatedTrainsTest {

    private DCCppSimulatorAdapter adapter;
    private DCCppSystemConnectionMemo memo;
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

    /** Blocks A, B, C, D in a line, with sensors DS101 to DS104, the last inverted if asked. */
    private Block[] line(boolean lastInverted) throws JmriException {
        String[] names = {"A", "B", "C", "D"};
        Block[] b = SimulationTestUtil.line(500, names);
        for (int i = 0; i < b.length; i++) {
            Sensor s = memo.getSensorManager().provideSensor("DS" + (101 + i));
            String name = names[i];
            boolean inverted = lastInverted && i == b.length - 1;
            ThreadingUtil.runOnLayoutWithJmriException(() -> {
                s.setUserName(name);
                s.setInverted(inverted);
                s.setKnownState(Sensor.INACTIVE);
            });
            b[i].setSensor(s.getSystemName());
            s.addPropertyChangeListener(recorder);
            recorded.add(s);
        }
        return b;
    }

    private DccThrottle throttle(int address) {
        AtomicReference<DccThrottle> found = new AtomicReference<>();
        ThrottleListener l = new ThrottleListener() {
            @Override
            public void notifyThrottleFound(DccThrottle t) {
                found.set(t);
            }

            @Override
            public void notifyFailedThrottleRequest(LocoAddress a, String reason) {
                throw new IllegalStateException("throttle for " + a + " refused: " + reason);
            }

            @Override
            public void notifyDecisionRequired(LocoAddress a, DecisionType question) {
                throw new IllegalStateException("throttle for " + a + " needs a decision: " + question);
            }
        };
        ThreadingUtil.runOnLayout(
                () -> memo.getThrottleManager().requestThrottle(new DccLocoAddress(address, false), l, false));
        JUnitUtil.waitFor(() -> found.get() != null, "throttle for " + address);
        return found.get();
    }

    private static void assertBefore(List<String> list, String first, String then) {
        int a = list.indexOf(first);
        int b = list.indexOf(then);
        assertTrue(a >= 0, first + " in " + list);
        assertTrue(b >= 0, then + " in " + list);
        assertTrue(a < b, first + " before " + then + " in " + list);
    }

    @Test
    public void testOffered() {
        DCCppSimulationAdapter a = new DCCppSimulationAdapter();
        assertEquals(List.of(DCCppSimulationAdapter.speedSource(adapter)), a.getSpeedSources());
        assertEquals(List.of(DCCppSimulationAdapter.sensorOutput(adapter)), a.getDetectorOutputs());
    }

    @Test
    public void testNotOfferedWhenTheOptionLeavesItOut() {
        assertTrue(adapter.isForSimulatedTrains(), "used by default");
        String[] choices = adapter.getOptionChoices(DCCppSimulatorAdapter.SIMULATED_TRAINS_OPTION);
        adapter.setOptionState(DCCppSimulatorAdapter.SIMULATED_TRAINS_OPTION, choices[1]);
        assertFalse(adapter.isForSimulatedTrains());
        DCCppSimulationAdapter a = new DCCppSimulationAdapter();
        assertTrue(a.getSpeedSources().isEmpty(), "no speed source");
        assertTrue(a.getDetectorOutputs().isEmpty(), "no sensor output");
    }

    @Test
    public void testReportsOnlyThisConnectionsNumberedSensors() throws JmriException {
        DCCppSimulatedSensorOutput out = DCCppSimulationAdapter.sensorOutput(adapter);
        Sensor ours = memo.getSensorManager().provideSensor("DS7");
        Sensor internal = InstanceManager.sensorManagerInstance().provideSensor("IS7");
        ThreadingUtil.runOnLayout(() -> {
            assertTrue(out.reports(ours));
            assertTrue(ours instanceof DCCppSensor);
            assertFalse(out.reports(internal), "not an Internal sensor");
        });
    }

    @Test
    public void testDccExThrottleDrivesTrainPastDccExSensors() throws JmriException {
        Block[] b = line(true);
        VirtualTrain t = SimulationTestUtil.train("T", 3, 100, 400);
        SimulationTestUtil.place(t, b[1], b[0], 150);
        JUnitUtil.waitFor(() -> changes.contains("B active"), "B reported by the command station");

        DccThrottle th = throttle(3);
        ThreadingUtil.runOnLayout(() -> {
            th.setIsForward(true);
            th.setSpeedSetting(0.5f);
        });
        SimulationTestUtil.waitForFrontIn(t, b[3]);
        ThreadingUtil.runOnLayout(() -> th.setSpeedSetting(0f));
        SimulationTestUtil.waitForTruth(tr -> {
            SimulationTruth.TrainTruth tt = tr.getTrain(t);
            return tt != null && tt.getSpeedMmPerSecond() == 0;
        }, "T stopped");
        JUnitUtil.waitFor(() -> changes.contains("D active"), "D reported, though its sensor is inverted");

        assertBefore(changes, "B active", "C active");
        assertBefore(changes, "C active", "B inactive");
        assertBefore(changes, "B inactive", "D active");
        assertFalse(changes.contains("A active"), "A never entered: " + changes);
        assertEquals(b[3], SimulationTestUtil.truth(t).getFrontBlock());
        assertEquals(Sensor.ACTIVE, recorded.get(3).getKnownState());
        ThreadingUtil.runOnLayout(() -> th.release(null));
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        // a profile with only this connection: fully simulated
        JUnitUtil.initConnectionConfigManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        adapter = new DCCppSimulatorAdapter();
        memo = adapter.getSystemConnectionMemo();
        ThreadingUtil.runOnLayout(adapter::configure);
        InstanceManager.getDefault(ConnectionConfigManager.class).add(new ConnectionConfig(adapter));
    }

    @AfterEach
    public void tearDown() {
        SimulationTestUtil.removeTrains();
        for (Sensor s : recorded) {
            s.removePropertyChangeListener(recorder);
        }
        memo.getDCCppTrafficController().terminateThreads();
        ThreadingUtil.runOnLayout(adapter::dispose);
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
