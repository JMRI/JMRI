package jmri.jmrit.logixng.actions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.expressions.ExpressionSensor;
import jmri.jmrit.logixng.implementation.DefaultConditionalNGScaffold;
import jmri.jmrit.logixng.util.LogixNG_Thread;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test Sequence: which steps run as its expressions change.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class SequenceTest {

    private Sensor start;
    private Sensor reset;
    private Sensor step1;
    private Sensor step2;
    // each step's action toggles a turnout, so each run of an action is a change of state
    private final List<String> stepsRun = new ArrayList<>();

    /** Set a sensor, and let the LogixNG thread finish what it set off: events run there in turn. */
    private static void set(Sensor sensor, int state) throws JmriException {
        sensor.setState(state);
        LogixNG_Thread.getThread(LogixNG_Thread.DEFAULT_LOGIXNG_THREAD).runOnLogixNG(() -> {
        });
    }

    private ExpressionSensor isActive(Sensor sensor) {
        ExpressionSensor e = new ExpressionSensor(
                InstanceManager.getDefault(DigitalExpressionManager.class).getAutoSystemName(), null);
        e.getSelectNamedBean().setNamedBean(sensor);
        e.getSelectEnum().setEnum(ExpressionSensor.SensorState.Active);
        return e;
    }

    private ActionTurnout toggle(String turnoutName, String step) {
        Turnout t = InstanceManager.getDefault(TurnoutManager.class).provide(turnoutName);
        t.setCommandedState(Turnout.CLOSED);
        t.addPropertyChangeListener(Turnout.PROPERTY_COMMANDED_STATE, e -> stepsRun.add(step));
        ActionTurnout a = new ActionTurnout(
                InstanceManager.getDefault(DigitalActionManager.class).getAutoSystemName(), null);
        a.getSelectNamedBean().setNamedBean(t);
        a.getSelectEnum().setEnum(ActionTurnout.TurnoutState.Toggle);
        return a;
    }

    /**
     * A Sequence of two steps, not run continuously, started by "Start" going Active, with steps that wait
     * for "Step 1" and "Step 2" to be Active.
     */
    private void twoStepSequence() throws SocketAlreadyConnectedException {
        twoStepSequence(false, false);
    }

    private void twoStepSequence(boolean startImmediately) throws SocketAlreadyConnectedException {
        twoStepSequence(startImmediately, false);
    }

    private void twoStepSequence(boolean startImmediately, boolean withReset) throws SocketAlreadyConnectedException {
        DigitalActionManager actions = InstanceManager.getDefault(DigitalActionManager.class);
        DigitalExpressionManager expressions = InstanceManager.getDefault(DigitalExpressionManager.class);
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A logixNG");
        ConditionalNG conditionalNG = new DefaultConditionalNGScaffold("IQC1", "A conditionalNG");
        InstanceManager.getDefault(ConditionalNG_Manager.class).register(conditionalNG);
        logixNG.addConditionalNG(conditionalNG);
        conditionalNG.setRunDelayed(false);
        conditionalNG.setEnabled(true);

        MaleSocket startIsActive = expressions.registerExpression(isActive(start));
        MaleSocket action1 = actions.registerAction(toggle("IT1", "step 1"));
        MaleSocket expression1 = expressions.registerExpression(isActive(step1));
        MaleSocket action2 = actions.registerAction(toggle("IT2", "step 2"));
        MaleSocket expression2 = expressions.registerExpression(isActive(step2));
        // two steps, as when loaded from a file
        Sequence sequence = new Sequence(actions.getAutoSystemName(), null,
                List.of(Map.entry("E1", expression1.getSystemName()), Map.entry("E2", expression2.getSystemName())),
                List.of(Map.entry("A1", action1.getSystemName()), Map.entry("A2", action2.getSystemName())));
        sequence.setRunContinuously(false);
        sequence.setStartImmediately(startImmediately);
        conditionalNG.getChild(0).connect(actions.registerAction(sequence));
        if (!startImmediately) {
            sequence.getChild(Sequence.EXPRESSION_START).connect(startIsActive);
        }
        if (withReset) {
            sequence.getChild(Sequence.EXPRESSION_RESET).connect(expressions.registerExpression(isActive(reset)));
        }
        sequence.getChild(3).connect(action1);
        sequence.getChild(4).connect(expression1);
        sequence.getChild(5).connect(action2);
        sequence.getChild(6).connect(expression2);

        assertTrue(logixNG.setParentForAllChildren(new ArrayList<>()));
        logixNG.activate();
        logixNG.setEnabled(true);
    }

    @Test
    public void testRunsOnceWhileStartStaysTrue() throws SocketAlreadyConnectedException, JmriException {
        twoStepSequence();
        set(start, Sensor.ACTIVE);
        assertEquals(List.of("step 1"), stepsRun, "started");
        set(step1, Sensor.ACTIVE);
        assertEquals(List.of("step 1", "step 2"), stepsRun, "the last step, and not started again while Start is true");
        set(step2, Sensor.ACTIVE);
        assertEquals(List.of("step 1", "step 2"), stepsRun, "not started again by a later event while Start is true");
    }

    @Test
    public void testStartsAgainWhenStartIsTrueAgain() throws SocketAlreadyConnectedException, JmriException {
        twoStepSequence();
        set(start, Sensor.ACTIVE);
        set(step1, Sensor.ACTIVE);
        assertEquals(List.of("step 1", "step 2"), stepsRun, "run to its end");
        set(step1, Sensor.INACTIVE);
        set(start, Sensor.INACTIVE);
        set(start, Sensor.ACTIVE);
        assertEquals(List.of("step 1", "step 2", "step 1"), stepsRun, "started again");
    }

    @Test
    public void testResetsOnceWhileResetStaysTrue() throws SocketAlreadyConnectedException, JmriException {
        twoStepSequence(false, true);
        set(start, Sensor.ACTIVE);
        assertEquals(List.of("step 1"), stepsRun, "started");
        set(reset, Sensor.ACTIVE);
        assertEquals(List.of("step 1", "step 1"), stepsRun, "reset: the first step again");
        set(step2, Sensor.ACTIVE);
        assertEquals(List.of("step 1", "step 1"), stepsRun, "not reset again by a later event while Reset is true");
        set(reset, Sensor.INACTIVE);
        set(reset, Sensor.ACTIVE);
        assertEquals(List.of("step 1", "step 1", "step 1"), stepsRun, "reset again when Reset is true again");
    }

    @Test
    public void testStartImmediatelyRunsOnce() throws SocketAlreadyConnectedException, JmriException {
        twoStepSequence(true);
        set(step1, Sensor.ACTIVE);
        assertEquals(List.of("step 1", "step 2"), stepsRun, "started by the first execution, and run to its end");
        set(step2, Sensor.ACTIVE);
        assertEquals(List.of("step 1", "step 2"), stepsRun, "not started again by a later execution");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initLogixNGManager();
        SensorManager sm = InstanceManager.getDefault(SensorManager.class);
        start = sm.provide("IS101");
        reset = sm.provide("IS104");
        step1 = sm.provide("IS102");
        step2 = sm.provide("IS103");
        for (Sensor s : List.of(start, reset, step1, step2)) {
            try {
                s.setState(Sensor.INACTIVE);
            } catch (JmriException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.tearDown();
    }
}
