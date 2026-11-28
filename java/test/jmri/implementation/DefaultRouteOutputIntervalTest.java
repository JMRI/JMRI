package jmri.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.*;

/**
 * Checks that the output sensors of Routes set at the same time are sent
 * spaced by the output interval when the connection keeps the interval.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class DefaultRouteOutputIntervalTest {

    private final List<Sensor> setBy = Collections.synchronizedList(new ArrayList<>());
    private final List<Long> setAt = Collections.synchronizedList(new ArrayList<>());
    private final List<Boolean> onLayoutThread = Collections.synchronizedList(new ArrayList<>());

    @Test
    public void testSensorsOfRoutesSetTogetherSpacedWhenIntervalKept() {
        int interval = 100;
        InstanceManager.getDefault(InternalSystemConnectionMemo.class).setOutputInterval(interval);
        InstanceManager.getDefault(InternalSystemConnectionMemo.class).setOutputIntervalKept(true);
        DefaultRoute first = route("IR1", "IS1", 4);
        DefaultRoute second = route("IR2", "IS2", 4);

        // as at start-up, two Routes set at the same time; without the queue each
        // Route spaces only its own sensors, so the two send their commands in pairs
        ThreadingUtil.runOnLayout(() -> {
            first.setRoute();
            second.setRoute();
        });

        JUnitUtil.waitFor(() -> setAt.size() == 8, "all sensors set");
        assertTrue(onLayoutThread.stream().allMatch(b -> b), "sensors set on the layout thread");
        // each time is taken just after its command, so allow a little for that
        long marginMs = 10;
        for (int i = 1; i < 8; i++) {
            long gapMs = (setAt.get(i) - setAt.get(i - 1)) / 1_000_000L;
            assertTrue(gapMs >= interval - marginMs, "sensor " + setBy.get(i).getSystemName() + " set "
                + gapMs + " ms after the one before, interval " + interval + " ms");
        }
        // each Route's own sensors in order
        for (String prefix : List.of("IS1", "IS2")) {
            List<String> names = new ArrayList<>();
            for (Sensor s : setBy) {
                if (s.getSystemName().startsWith(prefix)) {
                    names.add(s.getSystemName());
                }
            }
            assertEquals(List.of(prefix + "0", prefix + "1", prefix + "2", prefix + "3"), names,
                "sensors of the Route in order");
        }
        JUnitUtil.waitFor(() -> !first.isRouteBusy() && !second.isRouteBusy(), "routes not busy");
    }

    @Test
    public void testSensorsSetWhenIntervalNotKept() {
        DefaultRoute first = route("IR1", "IS1", 3);
        DefaultRoute second = route("IR2", "IS2", 3);

        ThreadingUtil.runOnLayout(() -> {
            first.setRoute();
            second.setRoute();
        });

        JUnitUtil.waitFor(() -> setAt.size() == 6, "all sensors set");
        assertTrue(onLayoutThread.stream().allMatch(b -> b), "sensors set on the layout thread");
        JUnitUtil.waitFor(() -> !first.isRouteBusy() && !second.isRouteBusy(), "routes not busy");
    }

    /**
     * A Route with output sensors that are on the layout, which an internal
     * sensor is not.
     */
    private DefaultRoute route(String routeName, String sensorPrefix, int count) {
        DefaultRoute r = new DefaultRoute(routeName);
        SensorManager sm = InstanceManager.getDefault(SensorManager.class);
        for (int i = 0; i < count; i++) {
            Sensor s = new AbstractSensor(sensorPrefix + i) {
                @Override
                public void requestUpdateFromLayout() {
                    // nothing to do
                }
            };
            try {
                s.setKnownState(Sensor.INACTIVE);
            } catch (JmriException ex) {
                Assertions.fail("could not set " + s.getSystemName(), ex);
            }
            sm.register(s);
            s.addPropertyChangeListener(Sensor.PROPERTY_KNOWN_STATE, e -> record(s));
            r.addOutputSensor(s.getSystemName(), Sensor.ACTIVE);
        }
        return r;
    }

    private void record(@Nonnull Sensor s) {
        // the time first: the test goes on once every sensor is in setAt
        long time = System.nanoTime();
        synchronized (setAt) {
            onLayoutThread.add(ThreadingUtil.isLayoutThread());
            setBy.add(s);
            setAt.add(time);
        }
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalSensorManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
