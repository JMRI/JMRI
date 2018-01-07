package jmri.jmrit.display;

import java.awt.GraphicsEnvironment;
import jmri.Sensor;
import jmri.jmrit.catalog.NamedIcon;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.jemmy.operators.JComponentOperator;
import org.netbeans.jemmy.operators.JFrameOperator;

/**
 * Swing tests for the SensorIcon
 *
 * @author	Bob Jacobsen Copyright 2009, 2010
 * @author  Paul Bender Copyright 2017
 */
public class SensorIconWindowTest {

    @Test
    public void testPanelEditor() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        jmri.jmrit.display.panelEditor.PanelEditor panel
                = new jmri.jmrit.display.panelEditor.PanelEditor("SensorIconWindowTest.testPanelEditor");

        SensorIcon icon = new SensorIcon(panel);
        panel.putItem(icon);

        Sensor sn = jmri.InstanceManager.sensorManagerInstance().provideSensor("IS1");
        icon.setSensor("IS1");
        icon.setIcon("BeanStateUnknown", new NamedIcon("resources/icons/smallschematics/tracksegments/circuit-error.gif",
                "resources/icons/smallschematics/tracksegments/circuit-error.gif"));
        icon.setDisplayLevel(Editor.SENSORS);	//daboudreau added this for Win7

        panel.setVisible(true);

        Assert.assertEquals("initial state", Sensor.UNKNOWN, sn.getState());

        // Click icon change state to Active
        JComponentOperator co = new JComponentOperator(panel.getTargetPanel());
        int xloc = icon.getLocation().x + icon.getSize().width / 2;
        int yloc = icon.getLocation().y + icon.getSize().height / 2;
        co.clickMouse(xloc,yloc,1);

        // this will wait for WAITFOR_MAX_DELAY (15 seconds) max 
        // checking the condition every WAITFOR_DELAY_STEP (5 mSecs)
        // if it's still false after max wait it throws an assert.
        JUnitUtil.waitFor(() -> {
            return sn.getState() != Sensor.UNKNOWN;
        }, "state not still unknown after one click");

        Assert.assertEquals("state after one click", Sensor.INACTIVE, sn.getState());

        // Click icon change state to inactive
        co.clickMouse(xloc,yloc,1);

        JUnitUtil.waitFor(() -> {
            return sn.getState() != Sensor.INACTIVE;
        }, "state not still inactive after two clicks");

        Assert.assertEquals("state after two clicks", Sensor.ACTIVE, sn.getState());

        // close the panel editor frame
        JFrameOperator eo = new JFrameOperator(panel);
        eo.requestClose();

        // close the panel target frame.
        EditorFrameOperator to = new EditorFrameOperator(panel.getTargetFrame());
        to.closeFrameWithConfirmations();
    }

    @Test
    public void testLayoutEditor() throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            return; // can't Assume in TestCase
        }

        jmri.jmrit.display.layoutEditor.LayoutEditor panel
                = new jmri.jmrit.display.layoutEditor.LayoutEditor("SensorIconWindowTest.testLayoutEditor");

        SensorIcon icon = new SensorIcon(panel);
        panel.putItem(icon);

        Sensor sn = jmri.InstanceManager.sensorManagerInstance().provideSensor("IS1");
        icon.setSensor("IS1");

        icon.setIcon("BeanStateUnknown", new NamedIcon("resources/icons/smallschematics/tracksegments/circuit-error.gif",
                "resources/icons/smallschematics/tracksegments/circuit-error.gif"));

        icon.setDisplayLevel(Editor.SENSORS); //daboudreau added this for Win7

        panel.setVisible(true);

        Assert.assertEquals("initial state", Sensor.UNKNOWN, sn.getState());

        // Click icon change state to Active
        JComponentOperator co = new JComponentOperator(panel.getTargetPanel());
        int xloc = icon.getLocation().x + icon.getSize().width / 2;
        int yloc = icon.getLocation().y + icon.getSize().height / 2;
        co.clickMouse(xloc,yloc,1);

        // this will wait for WAITFOR_MAX_DELAY (15 seconds) max 
        // checking the condition every WAITFOR_DELAY_STEP (5 mSecs)
        // if it's still false after max wait it throws an assert.
        JUnitUtil.waitFor(() -> {
            return sn.getState() != Sensor.UNKNOWN;
        }, "state not still unknown after one click");

        // this will wait for WAITFOR_MAX_DELAY (15 seconds) max 
        // checking the condition every WAITFOR_DELAY_STEP (5 mSecs)
        // if it's still false after max wait it throws an assert.
        JUnitUtil.waitFor(() -> {
            return sn.getState() != Sensor.UNKNOWN;
        }, "state not still unknown after one click");

        Assert.assertEquals("state after one click", Sensor.INACTIVE, sn.getState());

        // Click icon change state to inactive
        co.clickMouse(xloc,yloc,1);

        JUnitUtil.waitFor(() -> {
            return sn.getState() != Sensor.INACTIVE;
        }, "state not still inactive after two clicks");

        Assert.assertEquals("state after two clicks", Sensor.ACTIVE, sn.getState());

        // close the panel editor frame
        EditorFrameOperator to = new EditorFrameOperator(panel);
        to.closeFrameWithConfirmations();
    }

    // The minimal setup for log4J
    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initShutDownManager();
    }

    @After
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }
}
