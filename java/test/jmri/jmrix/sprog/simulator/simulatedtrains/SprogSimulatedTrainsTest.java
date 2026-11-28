package jmri.jmrix.sprog.simulator.simulatedtrains;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import jmri.Block;
import jmri.DccLocoAddress;
import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.LocoAddress;
import jmri.Sensor;
import jmri.ThrottleListener;
import jmri.jmrit.simulation.SimulationTestUtil;
import jmri.jmrit.simulation.SimulationTruth;
import jmri.jmrit.simulation.VirtualTrain;
import jmri.jmrix.AbstractPortController;
import jmri.jmrix.ConnectionConfigManager;
import jmri.jmrix.sprog.SprogSystemConnectionMemo;
import jmri.jmrix.sprog.simulator.ConnectionConfig;
import jmri.jmrix.sprog.simulator.SimulatorAdapter;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.*;

/**
 * Simulated trains on a SPROG Simulator connection, made as the connection
 * preferences make it (a programmer with one throttle, the default),
 * driven by JMRI's SPROG throttle past Internal sensors.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
@Timeout(60)
public class SprogSimulatedTrainsTest {

    private SimulatorAdapter adapter;
    private SprogSystemConnectionMemo memo;

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

    @Test
    public void testOfferedUnlessTheSettingLeavesItOut() {
        SprogSimulationAdapter a = new SprogSimulationAdapter();
        assertEquals(List.of(SprogSimulationAdapter.speedSource(adapter)), a.getSpeedSources());
        assertTrue(a.getDetectorOutputs().isEmpty(), "a SPROG has no detection");
        String[] choices = adapter.getOptionChoices(AbstractPortController.SIMULATED_TRAINS_OPTION);
        adapter.setOptionState(AbstractPortController.SIMULATED_TRAINS_OPTION, choices[1]);
        assertTrue(a.getSpeedSources().isEmpty(), "left out");
    }

    @Test
    public void testSprogThrottleDrivesTrain() {
        Block[] b = SimulationTestUtil.line(500, "A", "B", "C", "D");
        VirtualTrain t = SimulationTestUtil.train("T", 3, 100, 400);
        SimulationTestUtil.place(t, b[1], b[0], 150);
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
        assertEquals(b[3], SimulationTestUtil.truth(t).getFrontBlock());
        JUnitUtil.waitFor(() -> b[3].getSensor().getKnownState() == Sensor.ACTIVE, "D's Internal sensor set");
        assertFalse(b[0].getSensor().getKnownState() == Sensor.ACTIVE, "A never entered");
        ThreadingUtil.runOnLayout(() -> th.release(null));
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        SimulationTestUtil.setUpSimulatedProfile();
        adapter = new SimulatorAdapter();
        memo = adapter.getSystemConnectionMemo();
        adapter.openPort("(None Selected)", "JMRI test");
        ThreadingUtil.runOnLayout(adapter::configure);
        InstanceManager.getDefault(ConnectionConfigManager.class).add(new ConnectionConfig(adapter));
    }

    @AfterEach
    public void tearDown() {
        SimulationTestUtil.removeTrains();
        ThreadingUtil.runOnLayout(adapter::dispose);
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
