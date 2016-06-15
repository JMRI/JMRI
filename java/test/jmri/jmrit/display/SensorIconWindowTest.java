package jmri.jmrit.display;

import java.util.List;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import jmri.Sensor;
import jmri.jmrit.catalog.NamedIcon;
import jmri.util.JUnitUtil;
import junit.extensions.jfcunit.TestHelper;
import junit.extensions.jfcunit.eventdata.EventDataConstants;
import junit.extensions.jfcunit.eventdata.MouseEventData;
import junit.extensions.jfcunit.finder.AbstractButtonFinder;
import junit.extensions.jfcunit.finder.DialogFinder;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Swing jfcUnit tests for the SensorIcon
 *
 * @author	Bob Jacobsen Copyright 2009, 2010
 */
public class SensorIconWindowTest extends jmri.util.SwingTestCase {

    @SuppressWarnings("unchecked") // DialogFinder not parameterized
    public void testPanelEditor() throws Exception {

        jmri.jmrit.display.panelEditor.PanelEditor panel
                = new jmri.jmrit.display.panelEditor.PanelEditor("SensorIconWindowTest.testPanelEditor");

        JComponent jf = panel.getTargetPanel();

        SensorIcon icon = new SensorIcon(panel);
        panel.putItem(icon);

        Sensor sn = jmri.InstanceManager.sensorManagerInstance().provideSensor("IS1");
        icon.setSensor("IS1");
        icon.setIcon("BeanStateUnknown", new NamedIcon("resources/icons/smallschematics/tracksegments/circuit-error.gif",
                "resources/icons/smallschematics/tracksegments/circuit-error.gif"));
        icon.setDisplayLevel(Editor.SENSORS);	//daboudreau added this for Win7

        panel.setVisible(true);
        //jf.setVisible(true);

        Assert.assertEquals("initial state", Sensor.UNKNOWN, sn.getState());

        // Click icon change state to Active
        java.awt.Point location = new java.awt.Point(
                icon.getLocation().x + icon.getSize().width / 2,
                icon.getLocation().y + icon.getSize().height / 2);

        getHelper().enterClickAndLeave(
                new MouseEventData(this,
                        jf, // component
                        1, // number clicks
                        EventDataConstants.DEFAULT_MOUSE_MODIFIERS, // modifiers
                        false, // isPopUpTrigger
                        10, // sleeptime
                        EventDataConstants.CUSTOM, // position
                        location
                ));

        Assert.assertEquals("state after one click", Sensor.INACTIVE, sn.getState());

        // Click icon change state to inactive
        getHelper().enterClickAndLeave(new MouseEventData(this, icon));

        Assert.assertEquals("state after two clicks", Sensor.ACTIVE, sn.getState());

        // if OK to here, close window
        TestHelper.disposeWindow(panel.getTargetFrame(), this);

        // that pops dialog, find and press Delete
        List<JDialog> dialogList = new DialogFinder(null).findAll(panel.getTargetFrame());
        JDialog d = dialogList.get(0);

        // Find the button that deletes the panel
        AbstractButtonFinder bf = new AbstractButtonFinder("Delete Panel");
        JButton button = (JButton) bf.find(d, 0);
        Assert.assertNotNull(button);

        // Click button to delete panel and close window
        getHelper().enterClickAndLeave(new MouseEventData(this, button));

        // that pops dialog, find and press Yes - Delete
        dialogList = new DialogFinder(null).findAll(panel.getTargetFrame());
        d = dialogList.get(0);

        // Find the button that deletes the panel
        bf = new AbstractButtonFinder("Yes - Dele");
        button = (JButton) bf.find(d, 0);
        Assert.assertNotNull(button);

        // Click button to delete panel and close window
        getHelper().enterClickAndLeave(new MouseEventData(this, button));

    }

    @SuppressWarnings("unchecked") // DialogFinder not parameterized
    public void testLayoutEditor() throws Exception {

        jmri.jmrit.display.layoutEditor.LayoutEditor panel
                = new jmri.jmrit.display.layoutEditor.LayoutEditor("SensorIconWindowTest.testLayoutEditor");

        JComponent jf = panel.getTargetPanel();

        SensorIcon icon = new SensorIcon(panel);
        panel.putItem(icon);

        Sensor sn = jmri.InstanceManager.sensorManagerInstance().provideSensor("IS1");
        icon.setSensor("IS1");

        icon.setIcon("BeanStateUnknown", new NamedIcon("resources/icons/smallschematics/tracksegments/circuit-error.gif",
                "resources/icons/smallschematics/tracksegments/circuit-error.gif"));

        icon.setDisplayLevel(Editor.SENSORS); //daboudreau added this for Win7

        panel.setVisible(true);
        //jf.setVisible(true);

        Assert.assertEquals("initial state", Sensor.UNKNOWN, sn.getState());

        // Click icon change state to Active
        java.awt.Point location = new java.awt.Point(
                icon.getLocation().x + icon.getSize().width / 2,
                icon.getLocation().y + icon.getSize().height / 2);

        getHelper().enterClickAndLeave(
                new MouseEventData(this,
                        jf, // component
                        1, // number clicks
                        EventDataConstants.DEFAULT_MOUSE_MODIFIERS, // modifiers
                        false, // isPopUpTrigger
                        10, // sleeptime
                        EventDataConstants.CUSTOM, // position
                        location
                ));

        Assert.assertEquals("state after one click", Sensor.INACTIVE, sn.getState());

        // Click icon change state to inactive
        getHelper().enterClickAndLeave(new MouseEventData(this, icon));

        Assert.assertEquals("state after two clicks", Sensor.ACTIVE, sn.getState());

        // if OK to here, close window
        TestHelper.disposeWindow(panel.getTargetFrame(), this);

        // that pops dialog, find and press Delete
        List<JDialog> dialogList = new DialogFinder(null).findAll(panel.getTargetFrame());
        JDialog d = dialogList.get(0);

        // Find the button that deletes the panel
        AbstractButtonFinder bf = new AbstractButtonFinder("Delete Panel");
        JButton button = (JButton) bf.find(d, 0);
        Assert.assertNotNull(button);

        // Click button to delete panel and close window
        getHelper().enterClickAndLeave(new MouseEventData(this, button));

        // that pops dialog, find and press Yes - Delete
        dialogList = new DialogFinder(null).findAll(panel.getTargetFrame());
        d = dialogList.get(0);

        // Find the button that deletes the panel
        bf = new AbstractButtonFinder("Yes - Dele");
        button = (JButton) bf.find(d, 0);
        Assert.assertNotNull(button);

        // Click button to delete panel and close window
        getHelper().enterClickAndLeave(new MouseEventData(this, button));

    }

    // from here down is testing infrastructure
    public SensorIconWindowTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", SensorIconWindowTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(SensorIconWindowTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() throws Exception {
        super.setUp();
        apps.tests.Log4JFixture.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initShutDownManager();
    }

    protected void tearDown() throws Exception {
        apps.tests.Log4JFixture.tearDown();
        super.tearDown();
    }
}
