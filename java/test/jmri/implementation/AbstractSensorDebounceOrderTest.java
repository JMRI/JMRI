package jmri.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.swing.SwingUtilities;

import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.*;

/**
 * Checks that a debounced sensor only changes its known state together with
 * the event to listeners, and that a pulse shorter than the debounce time
 * does not get through while the layout thread is busy.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class AbstractSensorDebounceOrderTest {

    private AbstractSensor s;
    private List<String> events;

    @Test
    public void testShortPulseIgnoredWhileLayoutThreadBusy() throws Exception {
        s.setSensorDebounceGoingActiveTimer(50);

        // keep the layout thread busy, e.g. with a repaint
        CountDownLatch release = new CountDownLatch(1);
        SwingUtilities.invokeLater(() -> {
            try {
                release.await();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        });

        try {
            s.setOwnState(Sensor.ACTIVE);
            Thread.sleep(150); // longer than the debounce time
            assertEquals(Sensor.INACTIVE, s.getKnownState(), "known state changed before any event");

            s.setOwnState(Sensor.INACTIVE); // pulse ends
        } finally {
            release.countDown();
        }
        Thread.sleep(100); // let anything still due run
        ThreadingUtil.runOnLayout(() -> { });

        assertEquals(Sensor.INACTIVE, s.getKnownState(), "final known state");
        assertEquals("[]", events.toString(), "known state events for a short pulse");
    }

    @Test
    public void testDebouncedChangeSentOnLayoutThread() {
        s.setSensorDebounceGoingActiveTimer(30);
        s.setSensorDebounceGoingInActiveTimer(30);
        List<Boolean> consistent = Collections.synchronizedList(new ArrayList<>());
        s.addPropertyChangeListener(Sensor.PROPERTY_KNOWN_STATE, e ->
                consistent.add(ThreadingUtil.isLayoutThread() && e.getNewValue().equals(s.getKnownState())));

        s.setOwnState(Sensor.ACTIVE);
        assertEquals(Sensor.INACTIVE, s.getKnownState(), "before the debounce time");
        // The known state is set before the listeners are told, so wait for
        // the listener, not for the state.
        JUnitUtil.waitFor(() -> consistent.size() == 1, "listeners told after debounce");
        assertEquals(Sensor.ACTIVE, s.getKnownState(), "ACTIVE after debounce");

        // flapping: only the last raw state is applied
        s.setOwnState(Sensor.INACTIVE);
        s.setOwnState(Sensor.ACTIVE);
        s.setOwnState(Sensor.INACTIVE);
        JUnitUtil.waitFor(() -> consistent.size() == 2, "listeners told again after debounce");
        assertEquals(Sensor.INACTIVE, s.getKnownState(), "INACTIVE after debounce");

        assertEquals("[" + Sensor.INACTIVE + "->" + Sensor.ACTIVE + ", "
                + Sensor.ACTIVE + "->" + Sensor.INACTIVE + "]", events.toString(), "events");
        assertEquals(List.of(true, true), consistent, "events on layout thread with known state set");
    }

    @BeforeEach
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        JUnitUtil.initInternalSensorManager();
        s = (AbstractSensor) InstanceManager.getDefault(SensorManager.class).provideSensor("IS1");
        s.setOwnState(Sensor.INACTIVE);
        events = Collections.synchronizedList(new ArrayList<>());
        s.addPropertyChangeListener(Sensor.PROPERTY_KNOWN_STATE,
                e -> events.add(e.getOldValue() + "->" + e.getNewValue()));
    }

    @AfterEach
    public void tearDown() {
        s.dispose();
        JUnitUtil.tearDown();
    }

}
