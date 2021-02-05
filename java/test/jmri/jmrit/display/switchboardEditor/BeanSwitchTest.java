package jmri.jmrit.display.switchboardEditor;

import jmri.*;
import jmri.jmrit.beantable.AddNewDevicePanel;
import jmri.jmrit.display.EditorFrameOperator;
import org.junit.jupiter.api.*;
import java.awt.GraphicsEnvironment;
import java.awt.event.MouseEvent;
import java.util.Objects;

import jmri.util.JUnitUtil;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JFrameOperator;

import javax.swing.*;

/**
 *
 * @author Egbert Broerse Copyright (C) 2017, 2021
 */
@DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
public class BeanSwitchTest {

    private SwitchboardEditor swe = null;

    @Test
    public void testCTor() {
        SwitchboardEditor swe2 = new SwitchboardEditor("Bean Switch Default Switchboard");
        swe2.setSwitchType("T");
        swe2.setSwitchManu("I"); // set explicitly
        BeanSwitch t = new BeanSwitch(1,null,"IT1",0, swe2);
        Assertions.assertNotNull(t, "exists");
    }

    @Test
    public void testButtonConnected() {
        swe.setSwitchType("T");
        swe.setSwitchManu("I"); // set explicitly
        NamedBean nb = jmri.InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT2");
        BeanSwitch t = new BeanSwitch(1, nb, "IT2", SwitchboardEditor.BUTTON, swe);
        Assertions.assertNotNull(t, "exists");
        Assertions.assertTrue(t.getIconLabel().endsWith("?"), "Unknown label ?");
        t.displayState(2);
        Assertions.assertEquals("IT2: C", t.getIconLabel(), "Closed label C");
        t.displayState(4);
        Assertions.assertTrue(t.getIconLabel().endsWith("T"), "Thrown label T");

        Assertions.assertFalse(t.getInverted(), "IT2 not inverted"); // LT turnout could invert
        t.setBeanInverted(true);
        Assertions.assertFalse(t.getInverted(), "IT2 can't invert");
    }

    @Disabled("no output received in last 10 min on Travis CI GUI test run")
    @Test
        public void testSliderConnected() {
        swe.setSwitchType("T");
        swe.setSwitchManu("I"); // set explicitly
        NamedBean nb = jmri.InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT3");
        nb.setUserName("intTurnOne");
        BeanSwitch t = new BeanSwitch(1, nb, "IT3", SwitchboardEditor.SLIDER, swe);
        Assertions.assertNotNull(t, "exists");
        Assertions.assertNotNull(t.getIconLabel());
        Assertions.assertTrue(t.getIconLabel().endsWith("?"), "Unknown label ?");
        t.displayState(2);
        Assertions.assertEquals("IT3: C", t.getIconLabel(), "Closed label C");
        t.displayState(4);
        Assertions.assertTrue(t.getIconLabel().endsWith("T"), "Thrown label T");

        Thread dialog_thread1 = new Thread(() -> {
            JDialogOperator jdo = new JDialogOperator(Bundle.getMessage("EditNameTitle", ""));
            // step down for items inside JOptionPane
            ((JTextField)((JPanel)((JPanel)((JOptionPane)jdo.getContentPane().getComponent(0)).getComponent(0)).getComponent(0)).getComponent(1)).setText("intTurnThree");
            new JButtonOperator(jdo, Bundle.getMessage("ButtonOK")).doClick();
        });
        dialog_thread1.setName("Edit bean user name dialog");
        dialog_thread1.start();

        t.renameBeanDialog(); // dialog

        JUnitUtil.waitFor(()-> !(dialog_thread1.isAlive()), "Edit bean user name dialog");
        Assertions.assertEquals("intTurnThree", nb.getUserName(), "New name 3 reapplied to nb");
        // actual bean rename method
        t.renameBean("intTurnTwo", "intTurnThree");
        Assertions.assertEquals("intTurnTwo", t.getUserNameString(), "New name 2 applied to nb");

        t.cleanup(); // make sure no exception is thrown
    }

    @Test
    public void testLightKeyConnected() {
        NamedBean nb = jmri.InstanceManager.getDefault(LightManager.class).provideLight("IL4");
        nb.setUserName("intLightFour");
        swe.setSwitchType("L");
        swe.setSwitchManu("I"); // set explicitly
        BeanSwitch t = new BeanSwitch(1, nb, "IL4", SwitchboardEditor.KEY, swe);
        Assertions.assertNotNull(t, "exists");
        Assertions.assertNotNull(t.getIconLabel());
        Assertions.assertTrue(t.getIconLabel().endsWith("-"), "Initial Off label -");
        t.doMouseClicked(new MouseEvent(t, 1, 0, 0, 0, 0, 1, false));
        //t.displayState(nb.getState()); // we have no listener, so grab it directly
        Assertions.assertEquals("IL4: +", t.getIconLabel(), "On label +");

        t.cleanup(); // make sure no exception is thrown
    }

    @Disabled("no output received in last 10 min on Travis CI GUI test run")
    @Test
    public void testSensorSymbolUnconnected() {
        swe.setSwitchType("S");
        swe.setSwitchManu("I"); // set explicitly
        BeanSwitch t = new BeanSwitch(1, null, "IS4", SwitchboardEditor.SYMBOL, swe);
        Assertions.assertNotNull(t, "exists");
        Thread dialog_thread1 = new Thread(() -> {
            JFrameOperator jfo = new JFrameOperator(Bundle.getMessage("ConnectNewMenu", ""));
            // step down for items inside JFrame
            ((JTextField)((JPanel)((AddNewDevicePanel)jfo.getContentPane().getComponent(0)).getComponent(0)).getComponent(3)).setText("intSensFour");
            new JButtonOperator(jfo, Bundle.getMessage("ButtonOK")).doClick();
        });
        dialog_thread1.setName("Connect new Sensor dialog");
        dialog_thread1.start();

        t.connectNew(); // pops a dialog

        JUnitUtil.waitFor(()-> !(dialog_thread1.isAlive()), "Connect new Sensor dialog");
        // can't recreate this BeanSwitch to include a connection, so we just check sensor was created and available in the manager
        Sensor newSensor = jmri.InstanceManager.getDefault(SensorManager.class).getSensor("IS4");
        Assertions.assertNotNull(newSensor, "Sensor IS4 was created");
        String newName = Objects.requireNonNull(InstanceManager.getDefault(SensorManager.class).getSensor("IS4")).getUserName();
        Assertions.assertNotNull(newName, "Sensor IS4 has user name");
        Assertions.assertEquals("intSensFour", Objects.requireNonNull(InstanceManager.getDefault(SensorManager.class).getSensor("IS4")).getUserName(), "User name applied to nb");
        t.displayState(4);
        Assertions.assertEquals("IS4", t.getIconLabel(), "Active label (no sign until recreated as connected)");

        t.cleanup(); // make sure no exception is thrown
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        if (!GraphicsEnvironment.isHeadless()) {
            swe = new SwitchboardEditor("Bean Switch Test Switchboard");
        }
    }

    @AfterEach
    public void tearDown() {
        if (swe != null) {
            new EditorFrameOperator(swe.getTargetFrame()).closeFrameWithConfirmations();
            swe = null;
        }
        JUnitUtil.resetWindows(false,false);
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
