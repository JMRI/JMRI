package jmri.implementation;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for {@link jmri.Sensor.RawStateListener} on {@link AbstractSensor}.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class AbstractSensorRawStateListenerTest {

    private AbstractSensor s;
    private List<String> order;
    private List<Long> times;
    private Sensor.RawStateListener listener;

    @Test
    public void testToldOfEachChangeWithItsTimeBeforeTheKnownState() {
        s.setOwnState(Sensor.ACTIVE, 1000L);
        s.setOwnState(Sensor.ACTIVE, 2000L); // no change
        s.setOwnState(Sensor.INACTIVE, 3000L);

        assertEquals(List.of("raw 4->2", "known 4->2", "raw 2->4", "known 2->4"), order,
                "each raw change, then the known state");
        assertEquals(List.of(1000L, 3000L), times, "the times given");
    }

    @Test
    public void testPropertyListenersSeeNoNewEvent() {
        List<String> properties = Collections.synchronizedList(new ArrayList<>());
        s.addPropertyChangeListener(e -> properties.add(e.getPropertyName()));
        s.setOwnState(Sensor.ACTIVE, 1000L);
        s.setOwnState(Sensor.INACTIVE, 2000L);
        assertEquals(List.of(Sensor.PROPERTY_KNOWN_STATE, Sensor.PROPERTY_KNOWN_STATE), properties,
                "without a debounce, property listeners are told only of the known state, as before");
    }

    @Test
    public void testToldBeforeTheDebounceOfEveryChange() {
        s.setSensorDebounceGoingActiveTimer(200);
        s.setOwnState(Sensor.ACTIVE, 1000L);
        s.setOwnState(Sensor.INACTIVE, 2000L); // a pulse shorter than the debounce
        assertEquals(List.of("raw 4->2", "raw 2->4"), order, "both raw changes, at once");
        assertEquals(List.of(1000L, 2000L), times);
        assertEquals(Sensor.INACTIVE, s.getKnownState(), "the pulse never reaches the known state");

        s.setOwnState(Sensor.ACTIVE, 3000L);
        JUnitUtil.waitFor(() -> order.size() == 4, "known state after the debounce");
        assertEquals(List.of("raw 4->2", "raw 2->4", "raw 4->2", "known 4->2"), order);
        assertEquals(List.of(1000L, 2000L, 3000L), times,
                "the change is dated when it was received, not when the debounce ended");
    }

    @Test
    public void testSetWithoutATimeIsDatedNow() {
        long before = System.nanoTime();
        s.setOwnState(Sensor.ACTIVE);
        long after = System.nanoTime();
        assertEquals(1, times.size());
        assertTrue(times.get(0) >= before && times.get(0) <= after, "dated when set");
    }

    @Test
    public void testRemovedListenerNotTold() {
        s.removeRawStateListener(listener);
        s.setOwnState(Sensor.ACTIVE, 1000L);
        assertTrue(times.isEmpty());
        assertEquals(List.of("known 4->2"), order);
    }

    @Test
    public void testFailingListenerDoesNotStopTheOthersOrTheSensor() {
        s.removeRawStateListener(listener);
        s.addRawStateListener((sensor, oldState, newState, timeNanos) -> {
            throw new IllegalStateException("test");
        });
        s.addRawStateListener(listener);
        s.setOwnState(Sensor.ACTIVE, 1000L);
        JUnitAppender.assertErrorMessage("Raw state listener of Sensor \"IS1\" failed");
        assertEquals(List.of(1000L), times, "the next listener is still told");
        assertEquals(Sensor.ACTIVE, s.getKnownState(), "the sensor is still set");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initInternalSensorManager();
        s = (AbstractSensor) InstanceManager.getDefault(SensorManager.class).provideSensor("IS1");
        s.setOwnState(Sensor.INACTIVE);
        order = Collections.synchronizedList(new ArrayList<>());
        times = Collections.synchronizedList(new ArrayList<>());
        listener = (sensor, oldState, newState, timeNanos) -> {
            assertSame(s, sensor, "the sensor is given");
            assertEquals(newState, sensor.getRawState(), "the raw state is already set");
            order.add("raw " + oldState + "->" + newState);
            times.add(timeNanos);
        };
        s.addRawStateListener(listener);
        s.addPropertyChangeListener(Sensor.PROPERTY_KNOWN_STATE,
                e -> order.add("known " + e.getOldValue() + "->" + e.getNewValue()));
    }

    @AfterEach
    public void tearDown() {
        s.dispose();
        JUnitUtil.tearDown();
    }

}
