package jmri.jmrit.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.implementation.AbstractSensor;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for {@link InternalSensorOutput}.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class InternalSensorOutputTest {

    @Test
    public void testReportsInternalSensorsOnly() {
        InternalSensorOutput out = new InternalSensorOutput();
        Sensor internal = InstanceManager.getDefault(SensorManager.class).provideSensor("IS1");
        Sensor other = new AbstractSensor("XS1") {
            @Override
            public void requestUpdateFromLayout() {
            }
        };
        assertTrue(ThreadingUtil.runOnGUIwithReturn(() -> out.reports(internal)));
        assertFalse(ThreadingUtil.runOnGUIwithReturn(() -> out.reports(other)), "not in the internal manager");
    }

    @Test
    public void testReportGivesTheTime() {
        InternalSensorOutput out = new InternalSensorOutput();
        Sensor s = InstanceManager.getDefault(SensorManager.class).provideSensor("IS2");
        List<Long> times = new ArrayList<>();
        s.addRawStateListener((sensor, oldState, newState, timeNanos) -> times.add(timeNanos));
        out.report(s, Sensor.ACTIVE, 12345L);
        JUnitUtil.waitFor(() -> s.getKnownState() == Sensor.ACTIVE, "set on the layout thread");
        assertEquals(List.of(12345L), times);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initInternalSensorManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
