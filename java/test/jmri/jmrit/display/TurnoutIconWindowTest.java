package jmri.jmrit.display;

import java.awt.GraphicsEnvironment;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import jmri.NamedBeanHandle;
import jmri.Turnout;
import jmri.jmrit.catalog.NamedIcon;
import jmri.util.JUnitUtil;
import jmri.util.MathUtil;
import junit.extensions.jfcunit.TestHelper;
import junit.extensions.jfcunit.eventdata.EventDataConstants;
import junit.extensions.jfcunit.eventdata.MouseEventData;
import junit.extensions.jfcunit.finder.AbstractButtonFinder;
import junit.extensions.jfcunit.finder.DialogFinder;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing jfcUnit tests for the TurnoutIcon
 *
 * @author	Bob Jacobsen Copyright 2009, 2010
 */
public class TurnoutIconWindowTest extends jmri.util.SwingTestCase {
    
    public static final ResourceBundle rbean = ResourceBundle.getBundle("jmri.NamedBeanBundle");
    
    @SuppressWarnings("unchecked")
    public void testPanelEditor() throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            return; // can't Assume in TestCase
        }
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
        java.awt.Point location = MathUtil.pointForPoint2D(MathUtil.center(icon.getBounds()));
        
        getHelper().enterClickAndLeave(new MouseEventData(
                this,
                jf, // component
                1, // number clicks
                EventDataConstants.DEFAULT_MOUSE_MODIFIERS, // modifiers
                false, // isPopUpTrigger
                10, // sleeptime
                EventDataConstants.CUSTOM, // position
                location)
        );

        // this will wait for WAITFOR_MAX_DELAY (15 seconds) max
        // checking the condition every WAITFOR_DELAY_STEP (5 mSecs)
        // if it's still false after max wait it throws an assert.
        JUnitUtil.waitFor(() -> {
            return sn.getState() == Turnout.CLOSED;
        }, "state after one click");

        // Click icon change state to inactive
        getHelper().enterClickAndLeave(new MouseEventData(this, icon));
        JUnitUtil.waitFor(() -> {
            return sn.getState() == Turnout.THROWN;
        }, "state after two clicks");

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
        if (GraphicsEnvironment.isHeadless()) {
            return; // can't Assume in TestCase
        }
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
        java.awt.Point location = MathUtil.pointForPoint2D(MathUtil.center(icon.getBounds()));
        //System.out.println("location: " + location);
        
        getHelper().enterClickAndLeave(new MouseEventData(
                this,
                jf, // component
                1, // number clicks
                EventDataConstants.DEFAULT_MOUSE_MODIFIERS, // modifiers
                false, // isPopUpTrigger
                10, // sleeptime
                EventDataConstants.CUSTOM, // position
                location)
        );
        
        JUnitUtil.waitFor(() -> {
            return sn.getState() != Turnout.UNKNOWN;
        }, "Not initial state");
        
        JUnitUtil.waitFor(() -> {
            return sn.getState() == Turnout.CLOSED;
        }, "state after one click");

        // Click icon change state to inactive
        getHelper().enterClickAndLeave(new MouseEventData(this, icon));
        
        JUnitUtil.waitFor(() -> {
            return sn.getState() == Turnout.THROWN;
        }, "state after two clicks");

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
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(TurnoutIconWindowTest.class);
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        JUnitUtil.setUp();
        JUnitUtil.initShutDownManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalSensorManager();
    }
    
    @Override
    protected void tearDown() throws Exception {
        JUnitUtil.tearDown();
        super.tearDown();
    }
    private final static Logger log = LoggerFactory.getLogger(TurnoutIconWindowTest.class.getName());
}
