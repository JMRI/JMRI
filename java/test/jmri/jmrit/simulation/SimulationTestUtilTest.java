package jmri.jmrit.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Objects;

import jmri.Block;
import jmri.DccLocoAddress;
import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.Path;
import jmri.Sensor;
import jmri.jmrix.ConnectionConfig;
import jmri.jmrix.ConnectionConfigManager;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for {@link SimulationTestUtil}.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class SimulationTestUtilTest {

    @Test
    public void testSimulatedProfile() {
        ConnectionConfig[] connections = InstanceManager.getDefault(ConnectionConfigManager.class).getConnections();
        assertEquals(1, connections.length);
        assertTrue(connections[0] instanceof jmri.jmrix.internal.ConnectionConfig, "an Internal connection");
        assertTrue(SimulationCheck.isFullySimulated());
        assertTrue(InstanceManager.getDefault(jmri.ThrottleManager.class)
                instanceof jmri.jmrix.debugthrottle.DebugThrottleManager, "Internal throttles");
    }

    @Test
    public void testLine() {
        Block[] b = SimulationTestUtil.line(new String[]{"A", "B", "C"}, new double[]{100, 200, 300});
        assertEquals(3, b.length);
        assertEquals("IBB", b[1].getSystemName());
        assertEquals("B", b[1].getUserName());
        assertEquals(200.0, b[1].getLengthMm(), 1e-9);
        Sensor sensorB = Objects.requireNonNull(b[1].getSensor(), "B has a sensor");
        assertEquals("ISB", sensorB.getSystemName());
        assertEquals(Sensor.INACTIVE, sensorB.getKnownState());
        assertEquals(2, b[1].getPaths().size(), "B joins A and C");
        assertEquals(1, b[0].getPaths().size(), "A is an end");
        for (Path p : b[1].getPaths()) {
            assertTrue(p.getBlock() == b[0] || p.getBlock() == b[2]);
        }
        assertEquals(3, SimulationTestUtil.line(500, "D", "E", "F").length);
        assertThrows(IllegalArgumentException.class,
                () -> SimulationTestUtil.line(new String[]{"G"}, new double[]{1, 2}));
    }

    @Test
    public void testAddress() {
        assertEquals(new DccLocoAddress(127, false), SimulationTestUtil.address(127));
        assertEquals(new DccLocoAddress(128, true), SimulationTestUtil.address(128));
    }

    @Test
    public void testTrain() {
        VirtualTrain t = SimulationTestUtil.train("T", 3, 150, 250);
        assertEquals("T", t.getName());
        assertEquals(150.0, t.getLengthMm(), 1e-9);
        assertEquals(1, t.getLocos().size());
        assertEquals(new DccLocoAddress(3, false), t.getLocos().get(0).getAddress());
        assertEquals(Detection.Mode.ENTIRE_TRAIN, t.getDetection().getMode());
    }

    @Test
    public void testPlaceDriveAndRemove() {
        Block[] b = SimulationTestUtil.line(300, "A", "B", "C");
        VirtualTrain t = SimulationTestUtil.train("T", 3, 100, 200);
        SimulationTestUtil.place(t, b[1], b[0], 250);
        assertEquals(b[1], SimulationTestUtil.truth(t).getFrontBlock());
        DccThrottle throttle = SimulationTestUtil.throttle(3);
        assertNotNull(throttle);
        throttle.setSpeedSetting(0.5f);
        SimulationTestUtil.waitForFrontIn(t, b[2]);
        Sensor sensorC = Objects.requireNonNull(b[2].getSensor(), "C has a sensor");
        JUnitUtil.waitFor(() -> sensorC.getKnownState() == Sensor.ACTIVE, "C reported");
        throttle.setSpeedSetting(0.0f);
        SimulationTestUtil.waitForTruth(
                tr -> Objects.requireNonNull(tr.getTrain(t), "T placed").getSpeedMmPerSecond() == 0.0, "stopped");
        assertEquals(b[2], SimulationTestUtil.truth(t).getFrontBlock(), "stopped in C");
        SimulationTestUtil.removeTrains();
        assertTrue(SimulationTestUtil.layout().getTrains().isEmpty());
        assertFalse(SimulationTestUtil.layout().isMoverRunning(), "thread stopped");
        assertThrows(IllegalStateException.class, () -> SimulationTestUtil.truth(t), "not placed");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        SimulationTestUtil.setUpSimulatedProfile();
    }

    @AfterEach
    public void tearDown() {
        SimulationTestUtil.removeTrains();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
