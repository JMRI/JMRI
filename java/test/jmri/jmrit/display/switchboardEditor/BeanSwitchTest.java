package jmri.jmrit.display.switchboardEditor;

import jmri.LightManager;
import jmri.NamedBean;
import jmri.SensorManager;
import jmri.TurnoutManager;
import jmri.jmrit.display.EditorFrameOperator;
import jmri.util.JUnitAppender;
import org.junit.jupiter.api.*;
import java.awt.GraphicsEnvironment;
import java.awt.event.MouseEvent;

import jmri.util.JUnitUtil;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JFrameOperator;

/**
 *
 * @author Egbert Broerse Copyright (C) 2017, 2021
 */
public class BeanSwitchTest {

    private SwitchboardEditor swe = null;

    @Test
    public void testCTor() {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());
        BeanSwitch t = new BeanSwitch(1,null,"IT1",0, swe);
        Assertions.assertNotNull(t, "exists");
    }

    @Test
    public void testButtonConnected() {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());
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

    @Test
    public void testSliderConnected() {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());
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
            //((JTextField)jdo.getComponent(2)).setText("intTurnTwo");
            new JButtonOperator(jdo, Bundle.getMessage("ButtonOK")).doClick();
        });
        dialog_thread1.setName("Edit bean user name dialog");
        dialog_thread1.start();

        t.renameBeanDialog(); // dialog

        JUnitUtil.waitFor(()-> !(dialog_thread1.isAlive()), "Edit bean user name dialog");
        Assertions.assertEquals("intTurnOne", nb.getUserName(), "New name applied to nb");
        // actual bean rename method
        t.renameBean("intTurnTwo", "intTurnOne");
        Assertions.assertEquals("intTurnTwo", nb.getUserName(), "New name applied to nb");
    }

    @Test
    public void testLightKeyConnected() {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());
        NamedBean nb = jmri.InstanceManager.getDefault(LightManager.class).provideLight("IL4");
        nb.setUserName("intLightFour");
        BeanSwitch t = new BeanSwitch(1, nb, "IL4", SwitchboardEditor.KEY, swe);
        Assertions.assertNotNull(t, "exists");
        Assertions.assertNotNull(t.getIconLabel());
        Assertions.assertTrue(t.getIconLabel().endsWith("-"), "Initial Off label -");
        t.doMouseClicked(new MouseEvent(t, 1, 0, 0, 0, 0, 1, false));
        //t.displayState(nb.getState()); // we have no listener, so grab it directly
        Assertions.assertEquals("IL4: +", t.getIconLabel(), "On label +");

        t.cleanup(); // just make sure no exception is thrown
    }

    @Test
    public void testSensorSymbolUnconnected() {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());
        BeanSwitch t = new BeanSwitch(1, null, "IS4", SwitchboardEditor.SYMBOL, swe);
        Assertions.assertNotNull(t, "exists");
        Thread dialog_thread1 = new Thread(() -> {
            JFrameOperator jfo = new JFrameOperator(Bundle.getMessage("ConnectNewMenu", ""));
            new JButtonOperator(jfo, Bundle.getMessage("ButtonOK")).doClick();
        });
        dialog_thread1.setName("Connect new Sensor dialog");
        dialog_thread1.start();

        t.connectNew(); // pops a dialog

        JUnitUtil.waitFor(()-> !(dialog_thread1.isAlive()), "Connect new Sensor dialog");
        JUnitAppender.assertWarnMessage("switch IS4 not found on panel");
        JUnitAppender.assertWarnMessage("failed to update switch to state of IS4");
        // can't recreate this BeanSwitch to include a connection, so we just check it was created and available in the manager
        Assertions.assertNotNull(jmri.InstanceManager.getDefault(SensorManager.class).provideSensor("IS4"), "Sensor IS4 created");
        t.displayState(4);
        Assertions.assertEquals("IS4", t.getIconLabel(), "Active label (no sign)");
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
