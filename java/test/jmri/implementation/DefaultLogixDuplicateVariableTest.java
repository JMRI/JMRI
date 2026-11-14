package jmri.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.ArrayList;
import java.util.List;

import jmri.*;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Check that a Conditional using the same bean in more than one state
 * variable is calculated once per change of that bean.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class DefaultLogixDuplicateVariableTest {

    private int calculations;

    private DefaultLogix makeLogix(String name, List<ConditionalVariable> vars) {
        ConditionalManager cm = InstanceManager.getDefault(ConditionalManager.class);
        DefaultLogix x = new DefaultLogix(name, name, cm);
        DefaultConditional c = new DefaultConditional(name + "C1", "c1") {
            @Override
            public int calculate(boolean enable, java.beans.PropertyChangeEvent evt) {
                calculations++;
                return super.calculate(enable, evt);
            }
        };
        x.addConditional(c.getSystemName(), 0);
        x.addConditional(c.getSystemName(), c);
        c.setStateVariables(vars);
        c.setLogicType(Conditional.AntecedentOperator.ALL_OR, "");
        c.setTriggerOnChange(false);
        List<ConditionalAction> acts = new ArrayList<>();
        acts.add(new DefaultConditionalAction(Conditional.ACTION_OPTION_ON_CHANGE_TO_TRUE,
                Conditional.Action.SET_SENSOR, "IS11", Route.TOGGLE, ""));
        c.setAction(acts);
        return x;
    }

    @Test
    public void testSameSensorTwice() throws JmriException {
        Sensor s = InstanceManager.sensorManagerInstance().provideSensor("IS10");
        Sensor out = InstanceManager.sensorManagerInstance().provideSensor("IS11");
        s.setKnownState(Sensor.INACTIVE);
        out.setKnownState(Sensor.INACTIVE);

        List<ConditionalVariable> vars = new ArrayList<>();
        vars.add(new ConditionalVariable(false, Conditional.Operator.NONE,
                Conditional.Type.SENSOR_ACTIVE, "IS10", true));
        vars.add(new ConditionalVariable(false, Conditional.Operator.OR,
                Conditional.Type.SENSOR_INACTIVE, "IS10", true));
        makeLogix("IX10", vars).activateLogix();

        calculations = 0;
        int before = out.getKnownState();
        s.setKnownState(Sensor.ACTIVE);
        assertEquals(1, calculations, "one calculation for one sensor change");
        assertNotEquals(before, out.getKnownState(), "toggle action ran once");
    }

    @Test
    public void testSameSignalHeadTwoAppearances() throws JmriException {
        SignalHead h = new VirtualSignalHead("IH1");
        InstanceManager.getDefault(SignalHeadManager.class).register(h);
        h.setAppearance(SignalHead.RED);
        Sensor out = InstanceManager.sensorManagerInstance().provideSensor("IS11");
        out.setKnownState(Sensor.INACTIVE);

        List<ConditionalVariable> vars = new ArrayList<>();
        vars.add(new ConditionalVariable(false, Conditional.Operator.NONE,
                Conditional.Type.SIGNAL_HEAD_RED, "IH1", true));
        vars.add(new ConditionalVariable(false, Conditional.Operator.OR,
                Conditional.Type.SIGNAL_HEAD_GREEN, "IH1", true));
        makeLogix("IX11", vars).activateLogix();

        calculations = 0;
        int before = out.getKnownState();
        h.setAppearance(SignalHead.GREEN);   // leaves one watched appearance, enters the other
        assertEquals(1, calculations, "one calculation for one appearance change");
        assertNotEquals(before, out.getKnownState(), "toggle action ran once");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalSignalHeadManager();
        JUnitUtil.initLogixManager();
        JUnitUtil.initConditionalManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
