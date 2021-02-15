package jmri.jmrit.display.switchboardEditor;

import jmri.*;
//import jmri.jmrit.display.EditorFrameOperator;
import org.junit.jupiter.api.*;
//import java.awt.event.MouseEvent;
//import java.util.Objects;

import jmri.util.JUnitUtil;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
//import org.netbeans.jemmy.operators.JButtonOperator;
//import org.netbeans.jemmy.operators.JDialogOperator;
//import org.netbeans.jemmy.operators.JFrameOperator;
//import org.netbeans.jemmy.operators.JTextFieldOperator;

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
        SwitchboardEditor sbe = new SwitchboardEditor("Bean Switch Test Switchboard");
        sbe.setSwitchType("T");
        sbe.setSwitchManu("I"); // set explicitly
        BeanSwitch t = new BeanSwitch(1,null,"IT1",0, sbe);
        Assertions.assertNotNull(t, "exists");

        //new EditorFrameOperator(sbe.getTargetFrame()).closeFrameWithConfirmations();
    }

    @Test
    public void testButtonConnected() {
        SwitchboardEditor swe = new SwitchboardEditor("Bean Switch Test Switchboard");
        swe.setSwitchType("T");
        swe.setSwitchManu("I"); // set explicitly
        NamedBean nb = jmri.InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT2");
        BeanSwitch t = new BeanSwitch(2, nb, "IT2", SwitchboardEditor.BUTTON, swe);
        Assertions.assertNotNull(t, "exists");
        Assertions.assertTrue(t.getIconLabel().endsWith("?"), "Unknown label ?");
        t.displayState(2);
        Assertions.assertEquals("IT2: C", t.getIconLabel(), "Closed label C");
        t.displayState(4);
        Assertions.assertTrue(t.getIconLabel().endsWith("T"), "Thrown label T");

//        Assertions.assertFalse(t.getInverted(), "IT2 not inverted"); // LT turnout could invert
//        t.setBeanInverted(true);
//        Assertions.assertFalse(t.getInverted(), "IT2 can't invert");

        //new EditorFrameOperator(swe.getTargetFrame()).closeFrameWithConfirmations();
    }

    @Test
        public void testSliderConnected() {
        SwitchboardEditor sw2 = new SwitchboardEditor("Bean Switch Test Switchboard");
        sw2.setSwitchType("T");
        sw2.setSwitchManu("I"); // set explicitly
        NamedBean nb = jmri.InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT3");
        nb.setUserName("intTurnOne");
        BeanSwitch t = new BeanSwitch(3, nb, "IT3", SwitchboardEditor.SLIDER, sw2);
        Assertions.assertNotNull(t, "exists");
        Assertions.assertNotNull(t.getIconLabel());
        Assertions.assertTrue(t.getIconLabel().endsWith("?"), "Unknown label ?");
        t.displayState(2);
        Assertions.assertEquals("IT3: C", t.getIconLabel(), "Closed label C");
        t.displayState(4);
        Assertions.assertTrue(t.getIconLabel().endsWith("T"), "Thrown label T");

//        Thread dialog_thread1 = new Thread(() -> {
//            JDialogOperator jdo = new JDialogOperator(Bundle.getMessage("EditNameTitle", ""));
//            //new JTextFieldOperator(jdo, 0).setText("intTurnThree");
//            // option: step down for items inside JFrame - unstable for different UI, time out in Travis CI > 60 min
//            //((JTextField) ((JPanel) ((JPanel) ((JOptionPane) jdo.getContentPane().getComponent(0)).getComponent(0)).getComponent(0)).getComponent(1)).setText("intTurnThree");
//            new JButtonOperator(jdo, Bundle.getMessage("ButtonOK")).doClick();
//        });
//        dialog_thread1.setName("Edit bean user name dialog");
//        dialog_thread1.start();
//
//        t.renameBeanDialog(); // dialog
//
//        JUnitUtil.waitFor(() -> !(dialog_thread1.isAlive()), "Edit bean user name dialog");
//        Assertions.assertEquals("intTurnOne", nb.getUserName(), "New name 3 applied to nb");
//        // actual bean rename method
//        t.renameBean("intTurnTwo", "intTurnOne");
//        Assertions.assertEquals("intTurnTwo", t.getUserNameString(), "New name 2 applied to nb");

        //t.cleanup(); // make sure no exception is thrown
        //new EditorFrameOperator(sw2.getTargetFrame()).closeFrameWithConfirmations();
    }

    @Test
    public void testLightKeyConnected() {
        SwitchboardEditor sw3 = new SwitchboardEditor("Bean Switch Test Switchboard");
        NamedBean nb = jmri.InstanceManager.getDefault(LightManager.class).provideLight("IL4");
        nb.setUserName("intLightFour");
        sw3.setSwitchType("L");
        sw3.setSwitchManu("I"); // set explicitly
        BeanSwitch t = new BeanSwitch(4, nb, "IL4", SwitchboardEditor.KEY, sw3);
        Assertions.assertNotNull(t, "exists");
        Assertions.assertNotNull(t.getIconLabel());
        Assertions.assertTrue(t.getIconLabel().endsWith("-"), "Initial Off label -");
        // change state
        //t.doMouseClicked(new MouseEvent(t, 1, 0, 0, 0, 0, 1, false));
        //t.displayState(nb.getState()); // we have no listener, so grab it directly
        //Assertions.assertEquals("IL4: +", t.getIconLabel(), "On label +");

        //t.cleanup(); // make sure no exception is thrown
        //new EditorFrameOperator(sw3.getTargetFrame()).closeFrameWithConfirmations();
    }

    @Test
    public void testSensorSymbolUnconnected() {
        SwitchboardEditor sw4 = new SwitchboardEditor("Bean Switch Test Switchboard");
        sw4.setSwitchType("S");
        sw4.setSwitchManu("I"); // set explicitly
        BeanSwitch t = new BeanSwitch(5, null, "IS4", SwitchboardEditor.SYMBOL, sw4);
        Assertions.assertNotNull(t, "exists");
//        Thread dialog_thread1 = new Thread(() -> {
//            JFrameOperator jfo = new JFrameOperator(Bundle.getMessage("ConnectNewMenu", ""));
//            // no output received?: new JTextFieldOperator(jfo, 1).setText("intSensFour");
//            // option: step down for items inside JFrame - unstable for different UI, time out in Travis CI > 60 min
//            //((JTextField) ((JPanel) ((AddNewDevicePanel) jfo.getContentPane().getComponent(0)).getComponent(0)).getComponent(3)).setText("intSensFour");
//            new JButtonOperator(jfo, Bundle.getMessage("ButtonOK")).doClick();
//        });
//        dialog_thread1.setName("Connect new Sensor dialog");
//        dialog_thread1.start();
//
//        t.connectNew(); // pops a dialog
//
//        JUnitUtil.waitFor(() -> !(dialog_thread1.isAlive()), "Connect new Sensor dialog");
        // can't recreate this BeanSwitch to include a connection, so we just check sensor was created and available in the manager
//        Sensor newSensor = jmri.InstanceManager.getDefault(SensorManager.class).getSensor("IS4");
//        Assertions.assertNotNull(newSensor, "Sensor IS4 was created");
        // activate handling and testing user name when we can reliably set user name in dialog_thread1
        //String newName = Objects.requireNonNull(InstanceManager.getDefault(SensorManager.class).getSensor("IS4")).getUserName();
        //Assertions.assertNotNull(newName, "Sensor IS4 has user name");
        //Assertions.assertEquals("intSensFour", Objects.requireNonNull(InstanceManager.getDefault(SensorManager.class).getSensor("IS4")).getUserName(), "User name applied to nb");

        // try setting state:
//        t.displayState(4);
//        Assertions.assertEquals("IS4", t.getIconLabel(), "Active label (no sign until recreated as connected)");

        //t.cleanup(); // make sure no exception is thrown
        // new EditorFrameOperator(sw4.getTargetFrame()).closeFrameWithConfirmations();
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
