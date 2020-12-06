package jmri.jmrit.display.switchboardEditor;

import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.Objects;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.netbeans.jemmy.operators.JMenuOperator;

import jmri.*;
import jmri.jmrit.display.AbstractEditorTestBase;
import jmri.jmrit.display.EditorFrameOperator;
import jmri.util.ColorUtil;
import jmri.util.JUnitUtil;

/**
 * Test simple functioning of SwitchboardEditor
 *
 * @author Paul Bender Copyright (C) 2016
 * @author Egbert Broerse Copyright (C) 2017, 2020
 */
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SwitchboardEditorTest extends AbstractEditorTestBase<SwitchboardEditor> {

    //private static SwitchboardEditor e = null;

    @Test
    public void testDefaultCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SwitchboardEditor p = new SwitchboardEditor();
        Assert.assertNotNull("exists", p);
        p.dispose();
    }

    @Test
    public void testStringCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("exists", e);
    }

    @Test
    public void checkOptionsMenuExists() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        e.setVisible(true);
        EditorFrameOperator jfo = new EditorFrameOperator(e);
        JMenuOperator jmo = new JMenuOperator(jfo, Bundle.getMessage("MenuOptions"));
        Assert.assertNotNull("Options Menu Exists", jmo);
        Assert.assertEquals("Menu Item Count", 9, jmo.getItemCount());
    }

    @Test
    public void testIsDirty() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // defaults to false.
        Assert.assertFalse("isDirty", e.isDirty());
    }

    @Test
    public void testSetDirty() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // defaults to false, setDirty() sets it to true.
        e.setDirty();
        Assert.assertTrue("isDirty after set", e.isDirty());
    }

    @Test
    public void testSetDirtyWithParameter() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // defaults to false, so set it to true.
        e.setDirty(true);
        Assert.assertTrue("isDirty after set", e.isDirty());
    }

    @Test
    public void testResetDirty() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // defaults to false, so set it to true.
        e.setDirty(true);
        // then call resetDirty, which sets it back to false.
        e.resetDirty();
        Assert.assertFalse("isDirty after reset", e.isDirty());
    }

    @Test
    public void testGetSetDefaultTextColor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertEquals("Default Text Color", ColorUtil.ColorBlack, e.getDefaultTextColor());
        e.setDefaultTextColor(Color.PINK);
        Assert.assertEquals("Default Text Color after Set", ColorUtil.ColorPink, e.getDefaultTextColor());
    }

    @Test
    public void testSwitchRangeTurnouts() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertEquals("Default Switch Type Turnouts", "T", e.getSwitchType());
        Assert.assertEquals("Default Switch Shape Button", "button", e.getSwitchShape());
        Assert.assertEquals("Default Rows 0", 0, e.getRows()); // autoRows on
        e.setRows(10);
        Assert.assertEquals("Set Rows 10", 10, e.getRows());
        e.setShowUserName(false);
        Assert.assertEquals("Show User Name is No", "no", e.showUserName());
        e.setSwitchShape("symbol");
        Assert.assertEquals("Switch shape set to 'symbol'", "symbol", e.getSwitchShape());
        ((TurnoutManager) e.getManager('T')).provideTurnout("IT9"); // connect to item 1
        e.getSwitch("IT8").okAddPressed(new ActionEvent(e, ActionEvent.ACTION_PERFORMED, "test")); // to item 2
        e.setHideUnconnected(true); // speed up redraw
        e.updatePressed(); // rebuild for new Turnouts + symbol shape
        Assert.assertEquals("2 connected switches shown", 2, e.getTotal());
    }

    @Test
    public void testSwitchInvertSensors() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        e.setSwitchType("S");
        e.updatePressed(); // rebuild for Sensors
        Sensor sensor20 = ((SensorManager) e.getManager('S')).provideSensor("IS20");
        Assert.assertNotNull(jmri.InstanceManager.sensorManagerInstance().getSensor("IS20"));
        Objects.requireNonNull(InstanceManager.sensorManagerInstance().getSensor("IS20")).setUserName("twenty"); // make it harder to fetch label
        e.setHideUnconnected(true); // speed up redraw
        e.updatePressed(); // connect switch IS20 to Sensor IS20
        Assert.assertEquals("IS20 is Unknown", Sensor.UNKNOWN, sensor20.getState());
        Assert.assertNotNull(e.getSwitch("IS20"));
        Assert.assertEquals("IS20 is Inactive", "IS20: ?", e.getSwitch("IS20").getIconLabel());
        sensor20.setCommandedState(Sensor.ACTIVE);
        e.updatePressed(); // connect switch IS20 to Sensor IS20
        Assert.assertEquals("IS20 displays Active", "IS20: +", e.getSwitch("IS20").getIconLabel());
        e.getSwitch("IS20").doMouseClicked(new MouseEvent(e, 1, 0, 0, 0, 0, 1, false));
        Assert.assertEquals("IS20 is Inactive", Sensor.INACTIVE, sensor20.getState());
        Assert.assertEquals("IS20 displays Inactive", "IS20: -", e.getSwitch("IS20").getIconLabel());
        e.getSwitch("IS20").setBeanInverted(true);
        Assert.assertEquals("IS20 is Active", Sensor.ACTIVE, sensor20.getState());
        Assert.assertEquals("IS20 displays Active", "IS20: +", e.getSwitch("IS20").getIconLabel());
    }

    @Test
    public void testSwitchAllLights() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        e.setSwitchType("L");
        e.updatePressed(); // rebuild for Lights
        e.getSwitch("IL1").doMouseClicked(null); // no result, so nothing to test
        Assert.assertNull("Click on unconnected switch", e.getSwitch("IL1").getLight());
        Light light1 = ((LightManager) e.getManager('L')).provideLight("IL1");
        Assert.assertNotNull(jmri.InstanceManager.lightManagerInstance().getLight("IL1"));
        e.updatePressed(); // connect switch IL1 to Light IL1
        Assert.assertEquals("IL1 is Off", Light.OFF, light1.getState());

        Assert.assertNotNull(e.getSwitch("IL1"));
        e.getSwitch("IL1").doMouseClicked(new MouseEvent(e, 1, 0, 0, 0, 0, 1, false));
        Assert.assertEquals("IL1 is On", Light.ON, light1.getState());

        e.switchAllLights(Light.OFF);
        Assert.assertEquals("IL1 is Off via All", Light.OFF, light1.getState());

        Assert.assertEquals("Default 4x6 switches shown", 24, e.getTotal());
        e.setHideUnconnected(true);
        e.updatePressed(); // rebuild to hide unconnected
        Assert.assertEquals("1 connected switch shown", 1, e.getTotal());
    }

    // from here down is testing infrastructure

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalLightManager();
        if (!GraphicsEnvironment.isHeadless()) {
            e = new SwitchboardEditor("Switchboard Editor Test");
        }
    }

    @AfterEach
    @Override
    public void tearDown() {
        if (e != null) {
           // dispose on Swing thread
           new EditorFrameOperator(e.getTargetFrame()).closeFrameWithConfirmations();
           e = null;
        }
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
