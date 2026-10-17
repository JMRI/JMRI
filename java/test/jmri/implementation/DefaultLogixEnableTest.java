package jmri.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import jmri.*;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Check that enabling and disabling a Logix leaves its listeners in a state
 * that deActivateLogix() can clean up.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class DefaultLogixEnableTest {

    private Sensor in;
    private Sensor out;

    private DefaultLogix buildLogix(String name) {
        ConditionalManager cm = InstanceManager.getDefault(ConditionalManager.class);
        DefaultLogix x = new DefaultLogix(name, "Logix " + name, cm);
        DefaultConditional c = new DefaultConditional(name + "C1", "c1");
        x.addConditional(c.getSystemName(), 0);
        x.addConditional(c.getSystemName(), c);
        List<ConditionalVariable> vars = new ArrayList<>();
        vars.add(new ConditionalVariable(false, Conditional.Operator.NONE,
                Conditional.Type.SENSOR_ACTIVE, "IS1", true));
        c.setStateVariables(vars);
        List<ConditionalAction> acts = new ArrayList<>();
        acts.add(new DefaultConditionalAction(Conditional.ACTION_OPTION_ON_CHANGE_TO_TRUE,
                Conditional.Action.SET_SENSOR, "IS2", Sensor.ACTIVE, ""));
        c.setAction(acts);
        return x;
    }

    @Test
    public void testEnableThenDeactivateRemovesListeners() throws JmriException {
        int base = in.getNumPropertyChangeListeners();
        DefaultLogix x = buildLogix("IX1");

        // as when loaded disabled: never activated
        x.setEnabled(false);

        // enabled later from the Logix table
        x.setEnabled(true);
        assertEquals(base + 1, in.getNumPropertyChangeListeners(), "enabled Logix listens to its sensor");
        in.setKnownState(Sensor.ACTIVE);
        assertEquals(Sensor.ACTIVE, out.getKnownState(), "enabled Logix runs its action");

        // deactivated, as when the Logix is deleted or edited
        in.setKnownState(Sensor.INACTIVE);
        out.setKnownState(Sensor.INACTIVE);
        x.deActivateLogix();
        assertEquals(base, in.getNumPropertyChangeListeners(), "deactivated Logix removed its listener");
        in.setKnownState(Sensor.ACTIVE);
        assertEquals(Sensor.INACTIVE, out.getKnownState(), "deactivated Logix no longer acts");
    }

    @Test
    public void testDisableInactiveLogixAddsNoListeners() {
        int base = in.getNumPropertyChangeListeners();
        DefaultLogix x = buildLogix("IX3");
        x.setEnabled(false);
        assertEquals(base, in.getNumPropertyChangeListeners(),
                "disabling a Logix that is not active adds no listeners");
    }

    @Test
    public void testToggleActiveLogixStaysActive() throws JmriException {
        int base = in.getNumPropertyChangeListeners();
        DefaultLogix x = buildLogix("IX2");
        x.activateLogix();
        assertEquals(base + 1, in.getNumPropertyChangeListeners());

        x.setEnabled(false);
        x.setEnabled(true);
        assertEquals(base + 1, in.getNumPropertyChangeListeners(), "one listener after disable and enable");
        in.setKnownState(Sensor.ACTIVE);
        assertEquals(Sensor.ACTIVE, out.getKnownState(), "re-enabled Logix runs its action");

        x.deActivateLogix();
        assertEquals(base, in.getNumPropertyChangeListeners(), "deactivated Logix removed its listener");
    }

    @BeforeEach
    public void setUp() throws JmriException {
        JUnitUtil.setUp();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initLogixManager();
        JUnitUtil.initConditionalManager();
        in = InstanceManager.sensorManagerInstance().provideSensor("IS1");
        out = InstanceManager.sensorManagerInstance().provideSensor("IS2");
        in.setKnownState(Sensor.INACTIVE);
        out.setKnownState(Sensor.INACTIVE);
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
