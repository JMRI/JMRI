package jmri.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import jmri.*;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.apache.logging.log4j.core.LogEvent;
import org.junit.jupiter.api.*;

/**
 * Check that Logixes which trigger each other in a loop are stopped with an
 * error instead of running until the stack overflows, and that long chains
 * of Logixes that are not loops still run to the end.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class DefaultConditionalLoopTest {

    /**
     * A Logix that sets {@code out} to {@code whenTrue} when its Conditional
     * becomes true and to {@code whenFalse} when it becomes false. The
     * Conditional is true when all the {@code in} sensors are active.
     */
    private DefaultLogix makeLogix(String name, String out, int whenTrue, int whenFalse, String... in) {
        ConditionalManager cm = InstanceManager.getDefault(ConditionalManager.class);
        DefaultLogix x = new DefaultLogix(name, name, cm);
        DefaultConditional c = new DefaultConditional(name + "C1", "c1");
        x.addConditional(c.getSystemName(), 0);
        x.addConditional(c.getSystemName(), c);
        List<ConditionalVariable> vars = new ArrayList<>();
        for (int i = 0; i < in.length; i++) {
            vars.add(new ConditionalVariable(false, i == 0 ? Conditional.Operator.NONE : Conditional.Operator.AND,
                    Conditional.Type.SENSOR_ACTIVE, in[i], true));
        }
        c.setStateVariables(vars);
        List<ConditionalAction> acts = new ArrayList<>();
        acts.add(new DefaultConditionalAction(Conditional.ACTION_OPTION_ON_CHANGE_TO_TRUE,
                Conditional.Action.SET_SENSOR, out, whenTrue, ""));
        if (whenFalse != 0) {
            acts.add(new DefaultConditionalAction(Conditional.ACTION_OPTION_ON_CHANGE_TO_FALSE,
                    Conditional.Action.SET_SENSOR, out, whenFalse, ""));
        }
        c.setAction(acts);
        return x;
    }

    /**
     * Two Logixes that form an oscillator once IS22 is active: IX20 copies
     * IS20 to IS21 and IX21 sets IS20 to the opposite of IS21.
     */
    private Sensor makeOscillator(SensorManager sm) throws JmriException {
        Sensor gate = sm.provideSensor("IS22");
        for (String s : new String[]{"IS20", "IS21", "IS22"}) {
            sm.provideSensor(s).setKnownState(Sensor.INACTIVE);
        }
        makeLogix("IX20", "IS21", Sensor.ACTIVE, Sensor.INACTIVE, "IS20", "IS22").activateLogix();
        makeLogix("IX21", "IS20", Sensor.INACTIVE, Sensor.ACTIVE, "IS21").activateLogix();
        return gate;
    }

    @Test
    public void testLogixLoopIsStopped() throws JmriException {
        SensorManager sm = InstanceManager.sensorManagerInstance();
        Sensor gate = makeOscillator(sm);
        assertNull(JUnitAppender.checkForMessageStartingWith("Logix loop stopped"),
                "activating the Logixes does not start the loop");

        int[] seen = new int[1];
        gate.addPropertyChangeListener(Sensor.PROPERTY_KNOWN_STATE, e -> seen[0]++);

        // starts the loop; on master this throws StackOverflowError
        gate.setKnownState(Sensor.ACTIVE);

        assertEquals(1, seen[0], "a listener added after the Logix is still told about the change");
        LogEvent error = JUnitAppender.checkForMessageStartingWith("Logix loop stopped");
        assertNotNull(error, "loop is logged");
        String message = error.getMessage().getFormattedMessage();
        String loop = message.substring(message.lastIndexOf(": ") + 2);
        assertEquals(1, count(loop, "IX20C1"), "each Conditional in the loop is named once: " + message);
        assertEquals(1, count(loop, "IX21C1"), "each Conditional in the loop is named once: " + message);
    }

    @Test
    public void testLogixLoopIsStoppedAgain() throws JmriException {
        SensorManager sm = InstanceManager.sensorManagerInstance();
        Sensor gate = makeOscillator(sm);
        gate.setKnownState(Sensor.ACTIVE);
        assertNotNull(JUnitAppender.checkForMessageStartingWith("Logix loop stopped"), "loop is logged");

        // the Logixes are not disabled, so starting the loop again is
        // stopped and logged again
        gate.setKnownState(Sensor.INACTIVE);
        gate.setKnownState(Sensor.ACTIVE);
        assertNotNull(JUnitAppender.checkForMessageStartingWith("Logix loop stopped"), "loop is logged again");
    }

    @Test
    public void testLongLoopNamesLastConditionals() throws JmriException {
        SensorManager sm = InstanceManager.sensorManagerInstance();
        int length = 15;
        for (int i = 0; i < length; i++) {
            sm.provideSensor("IS" + (200 + i)).setKnownState(Sensor.INACTIVE);
        }
        Sensor gate = sm.provideSensor("IS299");
        gate.setKnownState(Sensor.INACTIVE);
        // a ring of Logixes, each copying its input to the next one, except
        // the first, which copies the opposite of the last one when the
        // gate is active
        makeLogix("IX200", "IS201", Sensor.INACTIVE, Sensor.ACTIVE, "IS200").activateLogix();
        for (int i = 1; i < length - 1; i++) {
            makeLogix("IX" + (200 + i), "IS" + (201 + i), Sensor.ACTIVE, Sensor.INACTIVE, "IS" + (200 + i))
                    .activateLogix();
        }
        makeLogix("IX" + (199 + length), "IS200", Sensor.ACTIVE, Sensor.INACTIVE, "IS" + (199 + length), "IS299")
                .activateLogix();

        gate.setKnownState(Sensor.ACTIVE);

        LogEvent error = JUnitAppender.checkForMessageStartingWith("Logix loop stopped");
        assertNotNull(error, "loop is logged");
        String message = error.getMessage().getFormattedMessage();
        String loop = message.substring(message.lastIndexOf(": ") + 2);
        assertTrue(loop.startsWith("... -> "), "earlier Conditionals are left out: " + message);
        assertEquals(10, count(loop, "C1"), "only the last Conditionals are named: " + message);
    }

    private static int count(String text, String part) {
        int n = 0;
        int i = text.indexOf(part);
        while (i >= 0) {
            n++;
            i = text.indexOf(part, i + part.length());
        }
        return n;
    }

    /**
     * A chain of 100 Logixes, each setting the input of the next one only
     * when its own input becomes active, is not a loop and runs to the end.
     */
    @Test
    public void testLongChainStillRuns() throws JmriException {
        SensorManager sm = InstanceManager.sensorManagerInstance();
        int length = 100;
        for (int i = 0; i <= length; i++) {
            sm.provideSensor("IS" + (1000 + i)).setKnownState(Sensor.INACTIVE);
        }
        for (int i = 0; i < length; i++) {
            makeLogix("IX" + (1000 + i), "IS" + (1001 + i), Sensor.ACTIVE, 0, "IS" + (1000 + i)).activateLogix();
        }
        Sensor end = sm.provideSensor("IS" + (1000 + length));
        assertEquals(Sensor.INACTIVE, end.getKnownState(), "the chain has not run yet");

        sm.provideSensor("IS1000").setKnownState(Sensor.ACTIVE);
        assertEquals(Sensor.ACTIVE, end.getKnownState(), "every Logix in the chain has run");
        assertNull(JUnitAppender.checkForMessageStartingWith("Logix loop stopped"), "no loop is reported");
    }

    /**
     * A ring of 200 Logixes is too long for the re-trigger check to stop
     * before the stack is used up. The stack overflow is fatal, so it is not
     * handled: it is logged in the outermost calculation, naming where the
     * calculations started, and passed on to the sensor change that started
     * them.
     */
    @Test
    public void testVeryLongLoopIsLogged() throws JmriException {
        SensorManager sm = InstanceManager.sensorManagerInstance();
        int length = 200;
        for (int i = 0; i < length; i++) {
            sm.provideSensor("IS" + (2000 + i)).setKnownState(Sensor.INACTIVE);
        }
        Sensor gate = sm.provideSensor("IS2999");
        gate.setKnownState(Sensor.INACTIVE);
        makeLogix("IX2000", "IS2001", Sensor.INACTIVE, Sensor.ACTIVE, "IS2000").activateLogix();
        for (int i = 1; i < length - 1; i++) {
            makeLogix("IX" + (2000 + i), "IS" + (2001 + i), Sensor.ACTIVE, Sensor.INACTIVE, "IS" + (2000 + i))
                    .activateLogix();
        }
        makeLogix("IX" + (1999 + length), "IS2000", Sensor.ACTIVE, Sensor.INACTIVE, "IS" + (1999 + length), "IS2999")
                .activateLogix();
        assertThrows(StackOverflowError.class, () -> gate.setKnownState(Sensor.ACTIVE),
                "the stack overflow is passed on");

        LogEvent error = JUnitAppender.checkForMessageStartingWith("Logix calculations started by");
        assertNotNull(error, "the stack overflow is logged");
        assertTrue(error.getMessage().getFormattedMessage().contains("IX2199C1"),
                "the message names where the calculations started: " + error.getMessage().getFormattedMessage());
        JUnitAppender.suppressErrorMessageStartsWith("Logix loop stopped");
    }

    /**
     * A Conditional that executes its actions whenever it is triggered, and
     * whose actions set several of its own inputs, is calculated again
     * inside its own calculation once for each input it changes. That ends
     * by itself and is not a loop.
     */
    @Test
    public void testSettingOwnInputsIsNotALoop() throws JmriException {
        SensorManager sm = InstanceManager.sensorManagerInstance();
        ConditionalManager cm = InstanceManager.getDefault(ConditionalManager.class);
        for (int i = 50; i <= 58; i++) {
            sm.provideSensor("IS" + i).setKnownState(Sensor.INACTIVE);
        }
        DefaultLogix x = new DefaultLogix("IX50", "IX50", cm);
        DefaultConditional c = new DefaultConditional("IX50C1", "c1");
        x.addConditional(c.getSystemName(), 0);
        x.addConditional(c.getSystemName(), c);
        List<ConditionalVariable> vars = new ArrayList<>();
        for (int i = 50; i <= 58; i++) {
            vars.add(new ConditionalVariable(false, i == 50 ? Conditional.Operator.NONE : Conditional.Operator.OR,
                    Conditional.Type.SENSOR_ACTIVE, "IS" + i, true));
        }
        c.setStateVariables(vars);
        c.setLogicType(Conditional.AntecedentOperator.ALL_OR, "");
        c.setTriggerOnChange(false);
        List<ConditionalAction> acts = new ArrayList<>();
        for (int i = 51; i <= 58; i++) {
            acts.add(new DefaultConditionalAction(Conditional.ACTION_OPTION_ON_CHANGE_TO_TRUE,
                    Conditional.Action.SET_SENSOR, "IS" + i, Sensor.ACTIVE, ""));
        }
        c.setAction(acts);
        x.activateLogix();

        sm.provideSensor("IS50").setKnownState(Sensor.ACTIVE);

        for (int i = 51; i <= 58; i++) {
            assertEquals(Sensor.ACTIVE, sm.provideSensor("IS" + i).getKnownState(), "IS" + i + " is set");
        }
        assertNull(JUnitAppender.checkForMessageStartingWith("Logix loop stopped"), "no loop is reported");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initLogixManager();
        JUnitUtil.initConditionalManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
