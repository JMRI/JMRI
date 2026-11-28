package jmri.jmrix.loconet.hexfile.simulatedtrains;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import jmri.Block;
import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Sensor;
import jmri.jmrit.simulation.SimulationTestUtil;
import jmri.jmrit.simulation.SimulationTruth;
import jmri.jmrit.simulation.VirtualTrain;
import jmri.jmrix.ConnectionConfigManager;
import jmri.jmrix.loconet.LnSensor;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.jmrix.loconet.hexfile.ConnectionConfig;
import jmri.jmrix.loconet.hexfile.HexFileFrame;
import jmri.jmrix.loconet.hexfile.LnHexFilePort;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 * Simulated trains on a LocoNet Simulator connection, made as the connection
 * preferences make it: driven by the simulator's own throttle and reporting
 * on LocoNet sensors with sensor input reports on the simulated LocoNet.
 * <p>
 * The trains move in real time, so these tests assert the order of events and
 * the ground truth, never how long anything took. The sensors are LS1 to LS4,
 * the last inverted.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
@Timeout(60)
@DisabledIfHeadless // opens the LocoNet Simulator window
public class LnSimulatedTrainsTest {

    private LnHexFilePort adapter;
    private HexFileFrame frame;
    private LocoNetSystemConnectionMemo memo;
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

    /** Blocks A, B, C, D in a line, with sensors LS1 to LS4, the last inverted. */
    private Block[] line() throws JmriException {
        String[] names = {"A", "B", "C", "D"};
        Block[] b = SimulationTestUtil.line(500, names);
        for (int i = 0; i < b.length; i++) {
            Sensor s = memo.getSensorManager().provideSensor("LS" + (1 + i));
            String name = names[i];
            boolean inverted = i == b.length - 1;
            ThreadingUtil.runOnLayoutWithJmriException(() -> {
                s.setUserName(name);
                s.setInverted(inverted);
                s.setKnownState(Sensor.INACTIVE);
            });
            b[i].setSensor(s.getSystemName());
            recorded.add(s);
        }
        // a LocoNet sensor takes the state it is set to from the echo of its input report
        for (Sensor s : recorded) {
            JUnitUtil.waitFor(() -> s.getKnownState() == Sensor.INACTIVE, s.getSystemName() + " inactive");
            s.addPropertyChangeListener(recorder);
        }
        return b;
    }

    private DccThrottle throttle(int address) {
        java.util.concurrent.atomic.AtomicReference<DccThrottle> found = new java.util.concurrent.atomic.AtomicReference<>();
        jmri.ThrottleListener l = new jmri.ThrottleListener() {
            @Override
            public void notifyThrottleFound(DccThrottle t) {
                found.set(t);
            }

            @Override
            public void notifyFailedThrottleRequest(jmri.LocoAddress a, String reason) {
                throw new IllegalStateException("throttle for " + a + " refused: " + reason);
            }

            @Override
            public void notifyDecisionRequired(jmri.LocoAddress a, DecisionType question) {
                throw new IllegalStateException("throttle for " + a + " needs a decision: " + question);
            }
        };
        ThreadingUtil.runOnLayout(() -> memo.getThrottleManager()
                .requestThrottle(new jmri.DccLocoAddress(address, false), l, false));
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
    public void testOfferedUnlessTheSettingLeavesItOut() {
        LnSimulationAdapter a = new LnSimulationAdapter();
        assertEquals(List.of(LnSimulationAdapter.speedSource(adapter.getSimulatedCommandStation())), a.getSpeedSources(),
                "its simulated command station's slots");
        assertEquals(List.of(LnSimulationAdapter.sensorOutput(adapter)), a.getDetectorOutputs());
        String[] choices = adapter.getOptionChoices(LnHexFilePort.SIMULATED_TRAINS_OPTION);
        adapter.setOptionState(LnHexFilePort.SIMULATED_TRAINS_OPTION, choices[1]);
        assertTrue(a.getDetectorOutputs().isEmpty(), "left out");
    }

    @Test
    public void testReportsOnlyThisConnectionsSensors() {
        LnSimulatedSensorOutput out = LnSimulationAdapter.sensorOutput(adapter);
        Sensor ours = memo.getSensorManager().provideSensor("LS7");
        Sensor internal = InstanceManager.sensorManagerInstance().provideSensor("IS7");
        ThreadingUtil.runOnLayout(() -> {
            assertTrue(ours instanceof LnSensor);
            assertTrue(out.reports(ours));
            assertFalse(out.reports(internal), "not an Internal sensor");
        });
    }

    @Test
    public void testLocoNetThrottleDrivesTrainPastLocoNetSensors() throws JmriException {
        Block[] b = line();
        VirtualTrain t = SimulationTestUtil.train("T", 3, 100, 400);
        SimulationTestUtil.place(t, b[1], b[0], 150);
        JUnitUtil.waitFor(() -> changes.contains("B active"), "B reported on LocoNet");

        DccThrottle th = throttle(3);
        assertTrue(th instanceof jmri.jmrix.loconet.LocoNetThrottle, "a LocoNet throttle, on a slot: " + th);
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
        ThreadingUtil.runOnLayout(() -> th.release(null));
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        SimulationTestUtil.setUpSimulatedProfile();
        adapter = new LnHexFilePort();
        memo = adapter.getSystemConnectionMemo();
        frame = new HexFileFrame(); // as LnHexFilePort.connect() makes it
        ThreadingUtil.runOnGUI(() -> {
            frame.setAdapter(adapter);
            frame.initComponents();
            frame.configure();
        });
        InstanceManager.getDefault(ConnectionConfigManager.class).add(new ConnectionConfig(adapter));
    }

    @AfterEach
    public void tearDown() {
        SimulationTestUtil.removeTrains();
        for (Sensor s : recorded) {
            s.removePropertyChangeListener(recorder);
        }
        adapter.close();
        frame.dispose();
        ((jmri.jmrix.loconet.LnPacketizer) memo.getLnTrafficController()).terminateThreads();
        ThreadingUtil.runOnLayout(adapter::dispose);
        JUnitUtil.removeMatchingThreads("LnPowerManager LnTrackStatusUpdateThread");
        JUnitUtil.removeMatchingThreads("LnSensorUpdateThread");
        JUnitUtil.removeMatchingThreads("LocoNet HexFileFrame");
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
