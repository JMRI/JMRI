package jmri.jmrit.display.switchboardEditor;

import java.awt.event.MouseEvent;

import jmri.*;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;

/**
 * Test functioning of switchboardeditor/BeanSwitch.
 * For now a lot of items turned off to find Travis CI problem.
 *
 * @author Egbert Broerse Copyright (C) 2017, 2021
 */
@DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
public class BeanSwitchTest {

    @Test
    public void testCTor() {
        BeanSwitch t = new BeanSwitch(1,null,"IT1",0, null);
        Assertions.assertNotNull(t, "exists");
    }

    @Test
    public void testTurnoutSwitch() {
        NamedBean nb = jmri.InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT4");
        BeanSwitch t = new BeanSwitch(4, nb, "IT5", SwitchboardEditor.KEY, null);
        Assertions.assertNotNull(t, "exists");
        Assertions.assertNotNull(t.getIconLabel());
        Assertions.assertEquals("IT5: ?", t.getIconLabel(), "Initial label -");
        t.displayState(Turnout.CLOSED);
        Assertions.assertEquals("IT5: C", t.getIconLabel(), "On label +");
        t.displayState(Turnout.THROWN);
        Assertions.assertEquals("IT5: T", t.getIconLabel(), "Off label -");
        t.displayState(Turnout.UNKNOWN);
        Assertions.assertEquals("IT5: ?", t.getIconLabel(), "Off label ?");
        t.displayState(Turnout.INCONSISTENT);
        Assertions.assertEquals("IT5: X", t.getIconLabel(), "Off label X");

        t = new BeanSwitch(4, nb, "IT58", SwitchboardEditor.SLIDER, null);
        Assertions.assertEquals("IT58: ?", t.getIconLabel(), "Initial label -");
        t.displayState(Turnout.CLOSED);
        Assertions.assertEquals("IT58: C", t.getIconLabel(), "On label +");
        t.displayState(Turnout.THROWN);
        Assertions.assertEquals("IT58: T", t.getIconLabel(), "Off label -");
        t.displayState(Turnout.UNKNOWN);
        Assertions.assertEquals("IT58: ?", t.getIconLabel(), "Off label ?");
        t.displayState(Turnout.INCONSISTENT);
        Assertions.assertEquals("IT58: X", t.getIconLabel(), "Off label X");

        t = new BeanSwitch(4, nb, "IT58", SwitchboardEditor.BUTTON, null);
        Assertions.assertEquals("IT58: ?", t.getIconLabel(), "Initial label -");
        t.displayState(Turnout.CLOSED);
        Assertions.assertEquals("IT58: C", t.getIconLabel(), "On label +");
        t.displayState(Turnout.THROWN);
        Assertions.assertEquals("IT58: T", t.getIconLabel(), "Off label -");
        t.displayState(Turnout.UNKNOWN);
        Assertions.assertEquals("IT58: ?", t.getIconLabel(), "Off label ?");
        t.displayState(Turnout.INCONSISTENT);
        Assertions.assertEquals("IT58: X", t.getIconLabel(), "Off label X");

        t = new BeanSwitch(4, nb, "IT1", SwitchboardEditor.ICONS, null);
        Assertions.assertEquals("IT1: ?", t.getIconLabel(), "Initial label -");
        t.displayState(Turnout.CLOSED);
        Assertions.assertEquals("IT1: C", t.getIconLabel(), "On label +");
        t.displayState(Turnout.THROWN);
        Assertions.assertEquals("IT1: T", t.getIconLabel(), "Off label -");
        t.setInverted(true); // test inverted display, C = T
        try {
            nb.setState(Turnout.THROWN);
        } catch (JmriException ignore) {
        }
        t.operate(new MouseEvent(t, 1, 0, 0, 0, 0, 1, false), "NAME");

        Assertions.assertEquals("IT1: C", t.getIconLabel(), "On label +");
        t.displayState(Turnout.UNKNOWN);
        Assertions.assertEquals("IT1: ?", t.getIconLabel(), "Off label ?");
        t.displayState(Turnout.INCONSISTENT);
        Assertions.assertEquals("IT1: X", t.getIconLabel(), "Off label X");

        t.cleanup(); // make sure no exception is thrown
    }

    @Test
    public void testLightSwitch() {
        NamedBean nb = jmri.InstanceManager.getDefault(LightManager.class).provideLight("IL4");
        BeanSwitch t = new BeanSwitch(4, nb, "IL4", SwitchboardEditor.KEY, null);
        Assertions.assertNotNull(t, "exists");
        Assertions.assertNotNull(t.getIconLabel());
        Assertions.assertEquals("IL4: -", t.getIconLabel(), "Initial label -");
        t.displayState(Light.ON);
        Assertions.assertEquals("IL4: +", t.getIconLabel(), "On label +");
        t.displayState(Light.OFF);
        Assertions.assertEquals("IL4: -", t.getIconLabel(), "Off label -");
        t.displayState(Light.UNKNOWN);
        Assertions.assertEquals("IL4: ?", t.getIconLabel(), "Off label ?");
        t.displayState(Light.INCONSISTENT);
        Assertions.assertEquals("IL4: X", t.getIconLabel(), "Off label X");

        t = new BeanSwitch(4, nb, "IL4", SwitchboardEditor.SLIDER, null);
        Assertions.assertEquals("IL4: -", t.getIconLabel(), "Initial label -");
        t.displayState(Light.ON);
        Assertions.assertEquals("IL4: +", t.getIconLabel(), "On label +");
        t.displayState(Light.OFF);
        Assertions.assertEquals("IL4: -", t.getIconLabel(), "Off label -");
        t.displayState(Light.UNKNOWN);
        Assertions.assertEquals("IL4: ?", t.getIconLabel(), "Off label ?");
        t.displayState(Light.INCONSISTENT);
        Assertions.assertEquals("IL4: X", t.getIconLabel(), "Off label X");

        t = new BeanSwitch(4, nb, "IL4", SwitchboardEditor.BUTTON, null);
        Assertions.assertEquals("IL4: -", t.getIconLabel(), "Initial label -");
        t.displayState(Light.ON);
        Assertions.assertEquals("IL4: +", t.getIconLabel(), "On label +");
        t.displayState(Light.OFF);
        Assertions.assertEquals("IL4: -", t.getIconLabel(), "Off label -");
        t.displayState(Light.UNKNOWN);
        Assertions.assertEquals("IL4: ?", t.getIconLabel(), "Off label ?");
        t.displayState(Light.INCONSISTENT);
        Assertions.assertEquals("IL4: X", t.getIconLabel(), "Off label X");

        t = new BeanSwitch(4, nb, "IL4", SwitchboardEditor.ICONS, null);
        Assertions.assertEquals("IL4: -", t.getIconLabel(), "Initial label -");
        t.displayState(Light.ON);
        Assertions.assertEquals("IL4: +", t.getIconLabel(), "On label +");
        t.displayState(Light.OFF);
        Assertions.assertEquals("IL4: -", t.getIconLabel(), "Off label -");
        t.displayState(Light.UNKNOWN);
        Assertions.assertEquals("IL4: ?", t.getIconLabel(), "Off label ?");
        t.displayState(Light.INCONSISTENT);
        Assertions.assertEquals("IL4: X", t.getIconLabel(), "Off label X");

        t.cleanup(); // make sure no exception is thrown
    }


    @Test
    public void testSensorSwitch() {
        Thread dialog_thread1 = new Thread(() -> {
            JDialogOperator jdo = new JDialogOperator(Bundle.getMessage("WarningTitle"));
            new JButtonOperator(jdo, Bundle.getMessage("ButtonOK")).doClick();
        });
        dialog_thread1.setName("Error dialog");
        dialog_thread1.start();

        BeanSwitch t = new BeanSwitch(3, null, "IP3", SwitchboardEditor.KEY, null);
        JUnitUtil.waitFor(() -> !(dialog_thread1.isAlive()), "Error dialog");
        JUnitAppender.assertErrorMessage("invalid char in Switchboard Button \"IP3\". Check connection name.");

        NamedBean nb = jmri.InstanceManager.getDefault(SensorManager.class).provideSensor("IS3");
        t = new BeanSwitch(3, nb, "IS3", SwitchboardEditor.KEY, null);
        Assertions.assertNotNull(t, "exists");
        Assertions.assertNotNull(t.getIconLabel());
        Assertions.assertEquals("IS3: ?", t.getIconLabel(), "Initial label -");
        t.displayState(Sensor.ACTIVE);
        Assertions.assertEquals("IS3: +", t.getIconLabel(), "On label +");
        t.displayState(Sensor.INACTIVE);
        Assertions.assertEquals("IS3: -", t.getIconLabel(), "Off label -");
        t.displayState(Sensor.UNKNOWN);
        Assertions.assertEquals("IS3: ?", t.getIconLabel(), "Off label ?");
        t.displayState(Sensor.INCONSISTENT);
        Assertions.assertEquals("IS3: X", t.getIconLabel(), "Off label X");

        t = new BeanSwitch(4, nb, "IS33", SwitchboardEditor.SLIDER, null);
        Assertions.assertEquals("IS33: ?", t.getIconLabel(), "Initial label -");
        t.displayState(Sensor.ACTIVE);
        Assertions.assertEquals("IS33: +", t.getIconLabel(), "On label +");
        t.displayState(Sensor.INACTIVE);
        Assertions.assertEquals("IS33: -", t.getIconLabel(), "Off label -");
        t.displayState(Sensor.UNKNOWN);
        Assertions.assertEquals("IS33: ?", t.getIconLabel(), "Off label ?");
        t.displayState(Sensor.INCONSISTENT);
        Assertions.assertEquals("IS33: X", t.getIconLabel(), "Off label X");

        t = new BeanSwitch(4, nb, "IS23", SwitchboardEditor.BUTTON, null);
        Assertions.assertEquals("IS23: ?", t.getIconLabel(), "Initial label -");
        t.displayState(Sensor.ACTIVE);
        Assertions.assertEquals("IS23: +", t.getIconLabel(), "On label +");
        t.displayState(Sensor.INACTIVE);
        Assertions.assertEquals("IS23: -", t.getIconLabel(), "Off label -");
        t.displayState(Sensor.UNKNOWN);
        Assertions.assertEquals("IS23: ?", t.getIconLabel(), "Off label ?");
        t.displayState(Sensor.INCONSISTENT);
        Assertions.assertEquals("IS23: X", t.getIconLabel(), "Off label X");

        t = new BeanSwitch(4, nb, "IS3", SwitchboardEditor.ICONS, null);
        Assertions.assertEquals("IS3: ?", t.getIconLabel(), "Initial label -");
        t.displayState(Sensor.ACTIVE);
        Assertions.assertEquals("IS3: +", t.getIconLabel(), "On label +");
        t.displayState(Sensor.INACTIVE);
        Assertions.assertEquals("IS3: -", t.getIconLabel(), "Off label -");
        t.displayState(Sensor.UNKNOWN);
        Assertions.assertEquals("IS3: ?", t.getIconLabel(), "Off label ?");
        t.displayState(Sensor.INCONSISTENT);
        Assertions.assertEquals("IS3: X", t.getIconLabel(), "Off label X");

        t.cleanup(); // make sure no exception is thrown
    }


    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalLightManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.deregisterEditorManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
