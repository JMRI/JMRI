package jmri.jmrit.display.switchboardEditor;

import java.awt.event.MouseEvent;

import jmri.*;
import jmri.util.*;
import jmri.util.swing.JemmyUtil;

import org.junit.jupiter.api.*;

import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test functioning of BeanSwitch.
 *
 * @author Egbert Broerse Copyright (C) 2017, 2021
 */
@jmri.util.junit.annotations.DisabledIfHeadless
public class BeanSwitchTest {

    @Test
    public void testCTor() {
        BeanSwitch t = new BeanSwitch(1,null,"IT1",0, null);
        assertNotNull(t, "exists");
    }

    @Test
    public void testTurnoutSwitch() {
        NamedBean nb = jmri.InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT4");
        BeanSwitch t = new BeanSwitch(4, nb, "IT5", SwitchboardEditor.KEY, null);
        assertNotNull(t, "exists");
        assertNotNull(t.getIconLabel());
        assertEquals("IT5: ?", t.getIconLabel(), "Initial label -");
        t.displayState(Turnout.CLOSED);
        assertEquals("IT5: C", t.getIconLabel(), "On label +");
        t.displayState(Turnout.THROWN);
        assertEquals("IT5: T", t.getIconLabel(), "Off label -");
        t.displayState(Turnout.UNKNOWN);
        assertEquals("IT5: ?", t.getIconLabel(), "Off label ?");
        t.displayState(Turnout.INCONSISTENT);
        assertEquals("IT5: X", t.getIconLabel(), "Off label X");

        t = new BeanSwitch(4, nb, "IT58", SwitchboardEditor.SLIDER, null);
        assertEquals("IT58: ?", t.getIconLabel(), "Initial label -");
        t.displayState(Turnout.CLOSED);
        assertEquals("IT58: C", t.getIconLabel(), "On label +");
        t.displayState(Turnout.THROWN);
        assertEquals("IT58: T", t.getIconLabel(), "Off label -");
        t.displayState(Turnout.UNKNOWN);
        assertEquals("IT58: ?", t.getIconLabel(), "Off label ?");
        t.displayState(Turnout.INCONSISTENT);
        assertEquals("IT58: X", t.getIconLabel(), "Off label X");

        t = new BeanSwitch(4, nb, "IT58", SwitchboardEditor.BUTTON, null);
        assertEquals("IT58: ?", t.getIconLabel(), "Initial label -");
        t.displayState(Turnout.CLOSED);
        assertEquals("IT58: C", t.getIconLabel(), "On label +");
        t.displayState(Turnout.THROWN);
        assertEquals("IT58: T", t.getIconLabel(), "Off label -");
        t.displayState(Turnout.UNKNOWN);
        assertEquals("IT58: ?", t.getIconLabel(), "Off label ?");
        t.displayState(Turnout.INCONSISTENT);
        assertEquals("IT58: X", t.getIconLabel(), "Off label X");

        t = new BeanSwitch(4, nb, "IT1", SwitchboardEditor.ICONS, null);
        assertEquals("IT1: ?", t.getIconLabel(), "Initial label -");
        t.displayState(Turnout.CLOSED);
        assertEquals("IT1: C", t.getIconLabel(), "On label +");
        t.displayState(Turnout.THROWN);
        assertEquals("IT1: T", t.getIconLabel(), "Off label -");
        t.setInverted(true); // test inverted display, C = T
        try {
            nb.setState(Turnout.THROWN);
        } catch (JmriException ignore) {
        }
        t.operate(new MouseEvent(t, 1, 0, 0, 0, 0, 1, false), "NAME");

        assertEquals("IT1: C", t.getIconLabel(), "On label +");
        t.displayState(Turnout.UNKNOWN);
        assertEquals("IT1: ?", t.getIconLabel(), "Off label ?");
        t.displayState(Turnout.INCONSISTENT);
        assertEquals("IT1: X", t.getIconLabel(), "Off label X");

        t.cleanup(); // make sure no exception is thrown
    }

    @Test
    public void testLightSwitch() {
        NamedBean nb = jmri.InstanceManager.getDefault(LightManager.class).provideLight("IL4");
        BeanSwitch t = new BeanSwitch(4, nb, "IL4", SwitchboardEditor.KEY, null);
        assertNotNull(t, "exists");
        assertNotNull(t.getIconLabel());
        assertEquals("IL4: -", t.getIconLabel(), "Initial label -");
        t.displayState(Light.ON);
        assertEquals("IL4: +", t.getIconLabel(), "On label +");
        t.displayState(Light.OFF);
        assertEquals("IL4: -", t.getIconLabel(), "Off label -");
        t.displayState(Light.UNKNOWN);
        assertEquals("IL4: ?", t.getIconLabel(), "Off label ?");
        t.displayState(Light.INCONSISTENT);
        assertEquals("IL4: X", t.getIconLabel(), "Off label X");

        t = new BeanSwitch(4, nb, "IL4", SwitchboardEditor.SLIDER, null);
        assertEquals("IL4: -", t.getIconLabel(), "Initial label -");
        t.displayState(Light.ON);
        assertEquals("IL4: +", t.getIconLabel(), "On label +");
        t.displayState(Light.OFF);
        assertEquals("IL4: -", t.getIconLabel(), "Off label -");
        t.displayState(Light.UNKNOWN);
        assertEquals("IL4: ?", t.getIconLabel(), "Off label ?");
        t.displayState(Light.INCONSISTENT);
        assertEquals("IL4: X", t.getIconLabel(), "Off label X");

        t = new BeanSwitch(4, nb, "IL4", SwitchboardEditor.BUTTON, null);
        assertEquals("IL4: -", t.getIconLabel(), "Initial label -");
        t.displayState(Light.ON);
        assertEquals("IL4: +", t.getIconLabel(), "On label +");
        t.displayState(Light.OFF);
        assertEquals("IL4: -", t.getIconLabel(), "Off label -");
        t.displayState(Light.UNKNOWN);
        assertEquals("IL4: ?", t.getIconLabel(), "Off label ?");
        t.displayState(Light.INCONSISTENT);
        assertEquals("IL4: X", t.getIconLabel(), "Off label X");

        t = new BeanSwitch(4, nb, "IL4", SwitchboardEditor.ICONS, null);
        assertEquals("IL4: -", t.getIconLabel(), "Initial label -");
        t.displayState(Light.ON);
        assertEquals("IL4: +", t.getIconLabel(), "On label +");
        t.displayState(Light.OFF);
        assertEquals("IL4: -", t.getIconLabel(), "Off label -");
        t.displayState(Light.UNKNOWN);
        assertEquals("IL4: ?", t.getIconLabel(), "Off label ?");
        t.displayState(Light.INCONSISTENT);
        assertEquals("IL4: X", t.getIconLabel(), "Off label X");

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
        assertNotNull(t);
        JUnitUtil.waitFor(() -> !(dialog_thread1.isAlive()), "Error dialog");
        JUnitAppender.assertErrorMessage("invalid char in Switchboard Button \"IP3\". Check connection name.");

        NamedBean nb = jmri.InstanceManager.getDefault(SensorManager.class).provideSensor("IS3");
        t = new BeanSwitch(3, nb, "IS3", SwitchboardEditor.KEY, null);
        assertNotNull(t, "exists");
        assertNotNull(t.getIconLabel());
        assertEquals("IS3: ?", t.getIconLabel(), "Initial label -");
        t.displayState(Sensor.ACTIVE);
        assertEquals("IS3: +", t.getIconLabel(), "On label +");
        t.displayState(Sensor.INACTIVE);
        assertEquals("IS3: -", t.getIconLabel(), "Off label -");
        t.displayState(Sensor.UNKNOWN);
        assertEquals("IS3: ?", t.getIconLabel(), "Off label ?");
        t.displayState(Sensor.INCONSISTENT);
        assertEquals("IS3: X", t.getIconLabel(), "Off label X");

        t = new BeanSwitch(4, nb, "IS33", SwitchboardEditor.SLIDER, null);
        assertEquals("IS33: ?", t.getIconLabel(), "Initial label -");
        t.displayState(Sensor.ACTIVE);
        assertEquals("IS33: +", t.getIconLabel(), "On label +");
        t.displayState(Sensor.INACTIVE);
        assertEquals("IS33: -", t.getIconLabel(), "Off label -");
        t.displayState(Sensor.UNKNOWN);
        assertEquals("IS33: ?", t.getIconLabel(), "Off label ?");
        t.displayState(Sensor.INCONSISTENT);
        assertEquals("IS33: X", t.getIconLabel(), "Off label X");

        t = new BeanSwitch(4, nb, "IS23", SwitchboardEditor.BUTTON, null);
        assertEquals("IS23: ?", t.getIconLabel(), "Initial label -");
        t.displayState(Sensor.ACTIVE);
        assertEquals("IS23: +", t.getIconLabel(), "On label +");
        t.displayState(Sensor.INACTIVE);
        assertEquals("IS23: -", t.getIconLabel(), "Off label -");
        t.displayState(Sensor.UNKNOWN);
        assertEquals("IS23: ?", t.getIconLabel(), "Off label ?");
        t.displayState(Sensor.INCONSISTENT);
        assertEquals("IS23: X", t.getIconLabel(), "Off label X");

        t = new BeanSwitch(4, nb, "IS3", SwitchboardEditor.ICONS, null);
        assertEquals("IS3: ?", t.getIconLabel(), "Initial label -");
        t.displayState(Sensor.ACTIVE);
        assertEquals("IS3: +", t.getIconLabel(), "On label +");
        t.displayState(Sensor.INACTIVE);
        assertEquals("IS3: -", t.getIconLabel(), "Off label -");
        t.displayState(Sensor.UNKNOWN);
        assertEquals("IS3: ?", t.getIconLabel(), "Off label ?");
        t.displayState(Sensor.INCONSISTENT);
        assertEquals("IS3: X", t.getIconLabel(), "Off label X");

        t.cleanup(); // make sure no exception is thrown
    }

    @Test
    public void testSetTurnoutUserName() {
        testSetUserName(InstanceManager.getDefault(TurnoutManager.class));
    }

    @Test
    public void testSetSensorUserName() {
        testSetUserName(InstanceManager.getDefault(SensorManager.class));
    }

    @Test
    public void testSetLightUserName() {
        testSetUserName(InstanceManager.getDefault(LightManager.class));
    }

    private static final String NEW_UNAME = "New UserName";
    private static final String EXISTING_UNAME = "Existing UserName";

    private void testSetUserName( ProvidingManager<?> mgr ) {

        NamedBean nbA = mgr.provide(mgr.getSystemNamePrefix()+11);
        NamedBean nbB = mgr.provide(mgr.getSystemNamePrefix()+12);
        nbB.setUserName(EXISTING_UNAME);

        var editor = new SwitchboardEditor("BeanSwitch TestSetUserName " + nbA.getBeanType());
        editor.setSwitchType(String.valueOf(mgr.typeLetter()));
        ThreadingUtil.runOnGUI( () -> editor.setVisible(true));

        BeanSwitch t = new BeanSwitch(4, nbA, nbA.getSystemName(), SwitchboardEditor.BUTTON, editor);
        ThreadingUtil.runOnGUI( () -> t.connectNew() );

        JFrameOperator jfo = new JFrameOperator(Bundle.getMessage("ConnectNewMenu", ""));
        assertNotNull(jfo);

        JTextFieldOperator jtfo = new JTextFieldOperator(jfo,1); // index 0 is SystemName
        assertNotNull(jtfo);
        jtfo.setText(NEW_UNAME);

        JButtonOperator jbo = new JButtonOperator(jfo, Bundle.getMessage("ButtonOK"));
        jbo.doClick();
        jbo.getQueueTool().waitEmpty();

        assertEquals(NEW_UNAME, nbA.getUserName());
        assertEquals(EXISTING_UNAME, nbB.getUserName());

        ThreadingUtil.runOnGUI( () -> t.connectNew() );

        jfo = new JFrameOperator(Bundle.getMessage("ConnectNewMenu", ""));
        assertNotNull(jfo);

        jtfo = new JTextFieldOperator(jfo,1); // index 0 is SystemName
        assertNotNull(jtfo);
        jtfo.setText(EXISTING_UNAME);

        Thread closeDialog = JemmyUtil.createModalDialogOperatorThread(
            Bundle.getMessage("WarningUserName", EXISTING_UNAME), Bundle.getMessage("ButtonOK"));

        jbo = new JButtonOperator(jfo, Bundle.getMessage("ButtonOK"));
        jbo.doClick();
        jbo.getQueueTool().waitEmpty();

        JUnitUtil.waitThreadTerminated(closeDialog);

        assertEquals(NEW_UNAME, nbA.getUserName());
        assertEquals(EXISTING_UNAME, nbB.getUserName());

        JUnitUtil.dispose(editor.getTargetFrame());
        JUnitUtil.dispose(editor);

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
        JUnitUtil.tearDown();
    }

}
