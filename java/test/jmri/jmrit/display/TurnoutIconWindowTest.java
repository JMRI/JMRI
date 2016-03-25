// TurnoutIconWindowTest.java
package jmri.jmrit.display;

import java.util.List;
import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import jmri.NamedBeanHandle;
import jmri.Turnout;
import jmri.jmrit.catalog.NamedIcon;
import junit.extensions.jfcunit.TestHelper;
import junit.extensions.jfcunit.eventdata.EventDataConstants;
import junit.extensions.jfcunit.eventdata.MouseEventData;
import junit.extensions.jfcunit.finder.AbstractButtonFinder;
import junit.extensions.jfcunit.finder.DialogFinder;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Swing jfcUnit tests for the TurnoutIcon
 *
 * @author	Bob Jacobsen Copyright 2009, 2010
 */
public class TurnoutIconWindowTest extends jmri.util.SwingTestCase {

    public static final ResourceBundle rbean = ResourceBundle.getBundle("jmri.NamedBeanBundle");

    @SuppressWarnings("unchecked")
    public void testPanelEditor() throws Exception {

        jmri.jmrit.display.panelEditor.PanelEditor panel
                = new jmri.jmrit.display.panelEditor.PanelEditor("TurnoutIconWindowTest.testPanelEditor");

        JComponent jf = panel.getTargetPanel();

        TurnoutIcon icon = new TurnoutIcon(panel);
        Turnout sn = jmri.InstanceManager.turnoutManagerInstance().provideTurnout("IT1");
        icon.setTurnout(new NamedBeanHandle<Turnout>("IT1", sn));

        icon.setDisplayLevel(Editor.TURNOUTS);

        icon.setIcon("TurnoutStateClosed",
                new NamedIcon("resources/icons/smallschematics/tracksegments/os-lefthand-east-closed.gif",
                        "resources/icons/smallschematics/tracksegments/os-lefthand-east-closed.gif"));
        icon.setIcon("TurnoutStateThrown",
                new NamedIcon("resources/icons/smallschematics/tracksegments/os-lefthand-east-thrown.gif",
                        "resources/icons/smallschematics/tracksegments/os-lefthand-east-thrown.gif"));
        icon.setIcon("BeanStateInconsistent",
                new NamedIcon("resources/icons/smallschematics/tracksegments/os-lefthand-east-error.gif",
                        "resources/icons/smallschematics/tracksegments/os-lefthand-east-error.gif"));
        icon.setIcon("BeanStateUnknown",
                new NamedIcon("resources/icons/smallschematics/tracksegments/os-lefthand-east-unknown.gif",
                        "resources/icons/smallschematics/tracksegments/os-lefthand-east-unknown.gif"));

        panel.putItem(icon);
        panel.setVisible(true);

        Assert.assertEquals("initial state", Turnout.UNKNOWN, sn.getState());

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

        Assert.assertEquals("state after one click", Turnout.CLOSED, sn.getState());

        // Click icon change state to inactive
        getHelper().enterClickAndLeave(new MouseEventData(this, icon));

        Assert.assertEquals("state after two clicks", Turnout.THROWN, sn.getState());

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

    @SuppressWarnings("unchecked")
    public void testLayoutEditor() throws Exception {

        jmri.jmrit.display.layoutEditor.LayoutEditor panel
                = new jmri.jmrit.display.layoutEditor.LayoutEditor("TurnoutIconWindowTest.testLayoutEditor");

        JComponent jf = panel.getTargetPanel();

        TurnoutIcon icon = new TurnoutIcon(panel);
        icon.setDisplayLevel(Editor.TURNOUTS);

        Turnout sn = jmri.InstanceManager.turnoutManagerInstance().provideTurnout("IT1");
        icon.setTurnout("IT1");

        icon.setIcon("TurnoutStateClosed",
                new NamedIcon("resources/icons/smallschematics/tracksegments/os-lefthand-east-closed.gif",
                        "resources/icons/smallschematics/tracksegments/os-lefthand-east-closed.gif"));
        icon.setIcon("TurnoutStateThrown",
                new NamedIcon("resources/icons/smallschematics/tracksegments/os-lefthand-east-thrown.gif",
                        "resources/icons/smallschematics/tracksegments/os-lefthand-east-thrown.gif"));
        icon.setIcon("BeanStateInconsistent",
                new NamedIcon("resources/icons/smallschematics/tracksegments/os-lefthand-east-error.gif",
                        "resources/icons/smallschematics/tracksegments/os-lefthand-east-error.gif"));
        icon.setIcon("BeanStateUnknown",
                new NamedIcon("resources/icons/smallschematics/tracksegments/os-lefthand-east-unknown.gif",
                        "resources/icons/smallschematics/tracksegments/os-lefthand-east-unknown.gif"));

        panel.putItem(icon);
        panel.setVisible(true);

        Assert.assertEquals("initial state", Turnout.UNKNOWN, sn.getState());

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

        Assert.assertEquals("state after one click", Turnout.CLOSED, sn.getState());

        // Click icon change state to inactive
        getHelper().enterClickAndLeave(new MouseEventData(this, icon));

        Assert.assertEquals("state after two clicks", Turnout.THROWN, sn.getState());

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
    public TurnoutIconWindowTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", TurnoutIconWindowTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(TurnoutIconWindowTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() throws Exception {
        super.setUp();
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initInternalTurnoutManager();
        jmri.util.JUnitUtil.initInternalSensorManager();
    }

    protected void tearDown() throws Exception {
        apps.tests.Log4JFixture.tearDown();
        super.tearDown();
    }
}
