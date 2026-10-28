package jmri.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import jmri.*;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Check that the timers of delayed sensor and turnout actions act on their
 * own action, and are stopped when the actions are replaced.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class DefaultConditionalDelayedActionTest {

    private final List<ConditionalAction> started = new ArrayList<>();

    private DefaultConditional makeConditional(List<ConditionalAction> actions) {
        DefaultConditional c = new DefaultConditional("IX1C1");
        List<ConditionalVariable> vars = new ArrayList<>();
        vars.add(new ConditionalVariable(false, Conditional.Operator.NONE,
                Conditional.Type.SENSOR_ACTIVE, "IS1", true));
        c.setStateVariables(vars);
        c.setAction(actions);
        return c;
    }

    private ConditionalAction delayed(Conditional.Action type, String device, int state) {
        // long delay, so the timer does not fire by itself during the test
        ConditionalAction a = new DefaultConditionalAction(Conditional.ACTION_OPTION_ON_CHANGE_TO_TRUE,
                type, device, state, "100");
        started.add(a);
        return a;
    }

    @Test
    public void testDelayedSensorActsOnItsOwnAction() throws JmriException {
        ConditionalAction delayedSensor = delayed(Conditional.Action.DELAYED_SENSOR, "IS2", Sensor.ACTIVE);
        ConditionalAction other = new DefaultConditionalAction(Conditional.ACTION_OPTION_ON_CHANGE_TO_FALSE,
                Conditional.Action.SET_SENSOR, "IS3", Sensor.ACTIVE, "");
        List<ConditionalAction> actions = new ArrayList<>();
        actions.add(delayedSensor);
        actions.add(other);
        DefaultConditional c = makeConditional(actions);

        InstanceManager.sensorManagerInstance().provideSensor("IS1").setKnownState(Sensor.ACTIVE);
        c.calculate(true, null);
        assertTrue(delayedSensor.isTimerActive(), "delayed sensor timer started");

        // the same actions in a different order
        List<ConditionalAction> reordered = new ArrayList<>();
        reordered.add(other);
        reordered.add(delayedSensor);
        c.setAction(reordered);
        assertTrue(delayedSensor.isTimerActive(), "timer of a kept action still runs");

        // the timer expires
        assertNotNull(delayedSensor.getListener());
        delayedSensor.getListener().actionPerformed(null);
        assertEquals(Sensor.ACTIVE, InstanceManager.sensorManagerInstance().provideSensor("IS2").getKnownState(),
                "delayed sensor set");
        assertEquals(Sensor.INACTIVE, InstanceManager.sensorManagerInstance().provideSensor("IS3").getKnownState(),
                "other sensor not touched");
        assertFalse(delayedSensor.isTimerActive(), "timer stopped after acting");
    }

    @Test
    public void testDelayedTurnoutActsOnItsOwnAction() throws JmriException {
        ConditionalAction delayedTurnout = delayed(Conditional.Action.DELAYED_TURNOUT, "IT1", Turnout.THROWN);
        ConditionalAction other = new DefaultConditionalAction(Conditional.ACTION_OPTION_ON_CHANGE_TO_FALSE,
                Conditional.Action.SET_TURNOUT, "IT2", Turnout.THROWN, "");
        List<ConditionalAction> actions = new ArrayList<>();
        actions.add(delayedTurnout);
        actions.add(other);
        DefaultConditional c = makeConditional(actions);

        InstanceManager.sensorManagerInstance().provideSensor("IS1").setKnownState(Sensor.ACTIVE);
        c.calculate(true, null);
        assertTrue(delayedTurnout.isTimerActive(), "delayed turnout timer started");

        List<ConditionalAction> reordered = new ArrayList<>();
        reordered.add(other);
        reordered.add(delayedTurnout);
        c.setAction(reordered);

        delayedTurnout.getListener().actionPerformed(null);
        assertEquals(Turnout.THROWN, InstanceManager.turnoutManagerInstance().provideTurnout("IT1").getCommandedState(),
                "delayed turnout set");
        assertEquals(Turnout.CLOSED, InstanceManager.turnoutManagerInstance().provideTurnout("IT2").getCommandedState(),
                "other turnout not touched");
    }

    @Test
    public void testReplacedActionTimerIsStopped() throws JmriException {
        ConditionalAction delayedSensor = delayed(Conditional.Action.DELAYED_SENSOR, "IS2", Sensor.ACTIVE);
        List<ConditionalAction> actions = new ArrayList<>();
        actions.add(delayedSensor);
        DefaultConditional c = makeConditional(actions);

        InstanceManager.sensorManagerInstance().provideSensor("IS1").setKnownState(Sensor.ACTIVE);
        c.calculate(true, null);
        assertTrue(delayedSensor.isTimerActive(), "delayed sensor timer started");

        // the actions are replaced, as when the Conditional is edited
        List<ConditionalAction> replacement = new ArrayList<>();
        replacement.add(delayed(Conditional.Action.DELAYED_SENSOR, "IS2", Sensor.ACTIVE));
        c.setAction(replacement);
        assertFalse(delayedSensor.isTimerActive(), "timer of the replaced action is stopped");
        assertFalse(delayedSensor.getTimer().isRunning(), "Swing timer of the replaced action is stopped");
    }

    @BeforeEach
    public void setUp() throws JmriException {
        JUnitUtil.setUp();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initLogixManager();
        JUnitUtil.initConditionalManager();
        for (String s : new String[]{"IS1", "IS2", "IS3"}) {
            InstanceManager.sensorManagerInstance().provideSensor(s).setKnownState(Sensor.INACTIVE);
        }
        for (String t : new String[]{"IT1", "IT2"}) {
            InstanceManager.turnoutManagerInstance().provideTurnout(t).setCommandedState(Turnout.CLOSED);
        }
    }

    @AfterEach
    public void tearDown() {
        for (ConditionalAction a : started) {
            a.stopTimer();
        }
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
