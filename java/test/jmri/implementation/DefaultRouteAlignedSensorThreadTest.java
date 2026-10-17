package jmri.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.*;

/**
 * Checks that setting a route changes its turnouts aligned sensor on the
 * layout thread.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class DefaultRouteAlignedSensorThreadTest {

    @Test
    public void testAlignedSensorSetOnLayoutThread() {
        Turnout t = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT1");
        t.setCommandedState(Turnout.CLOSED);
        Sensor aligned = InstanceManager.getDefault(SensorManager.class).provideSensor("IS1");

        DefaultRoute r = new DefaultRoute("IR1");
        r.addOutputTurnout("IT1", Turnout.CLOSED);
        r.setTurnoutsAlignedSensor("IS1");

        List<String> events = Collections.synchronizedList(new ArrayList<>());
        aligned.addPropertyChangeListener(Sensor.PROPERTY_KNOWN_STATE, e ->
                events.add(e.getNewValue() + (ThreadingUtil.isLayoutThread() ? "" : " off layout thread")));

        ThreadingUtil.runOnLayout(r::setRoute);
        JUnitUtil.waitFor(() -> aligned.getKnownState() == Sensor.ACTIVE, "aligned sensor ACTIVE");
        JUnitUtil.waitFor(() -> !r.isRouteBusy(), "route not busy");

        assertEquals("[" + Sensor.INCONSISTENT + ", " + Sensor.ACTIVE + "]", events.toString(),
                "aligned sensor changes");
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
