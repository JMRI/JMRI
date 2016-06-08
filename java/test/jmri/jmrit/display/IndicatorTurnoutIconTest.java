package jmri.jmrit.display;

import java.beans.PropertyChangeEvent;
import javax.swing.JButton;
import javax.swing.JFrame;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * IndicatorTurnoutIconTest.java
 *
 * @author	Bob Jacobsen
 */
public class IndicatorTurnoutIconTest extends jmri.util.SwingTestCase {

    IndicatorTurnoutIcon to;
    jmri.jmrit.display.panelEditor.PanelEditor panel;

    public void testEquals() {
        JFrame jf = new JFrame();
        jf.getContentPane().setLayout(new java.awt.FlowLayout());

        to = new IndicatorTurnoutIcon(panel);
        jmri.Turnout turnout = jmri.InstanceManager.turnoutManagerInstance().provideTurnout("IT1");
        to.setTurnout(new jmri.NamedBeanHandle<jmri.Turnout>("IT1", turnout));

        IndicatorTurnoutIcon to2 = new IndicatorTurnoutIcon(panel);
        turnout = jmri.InstanceManager.turnoutManagerInstance().provideTurnout("IT1");
        to2.setTurnout(new jmri.NamedBeanHandle<jmri.Turnout>("IT1", turnout));

        Assert.assertTrue("identity", to.equals(to));
        Assert.assertFalse("object (not content) equality", to2.equals(to));
        Assert.assertFalse("object (not content) equality commutes", to.equals(to2));        
    }

    public void testClone() {
        JFrame jf = new JFrame();
        jf.getContentPane().setLayout(new java.awt.FlowLayout());

        to = new IndicatorTurnoutIcon(panel);
        jmri.Turnout turnout = jmri.InstanceManager.turnoutManagerInstance().provideTurnout("IT1");
        to.setTurnout(new jmri.NamedBeanHandle<jmri.Turnout>("IT1", turnout));

        IndicatorTurnoutIcon to2 = (IndicatorTurnoutIcon) to.deepClone();

        Assert.assertFalse("clone object (not content) equality", to2.equals(to));
        
        Assert.assertTrue("class type equality", to2.getClass().equals(to.getClass()));
        
    }
    

    // from here down is testing infrastructure
    public IndicatorTurnoutIconTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", IndicatorTurnoutIconTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(IndicatorTurnoutIconTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    
        jmri.util.JUnitUtil.resetInstanceManager();
        panel = new jmri.jmrit.display.panelEditor.PanelEditor("Test IndicatorTurnoutIcon Panel");
        to = null;
    }

    protected void tearDown() {
        // now close panel window
        java.awt.event.WindowListener[] listeners = panel.getTargetFrame().getWindowListeners();
        for (int i = 0; i < listeners.length; i++) {
            panel.getTargetFrame().removeWindowListener(listeners[i]);
        }
        junit.extensions.jfcunit.TestHelper.disposeWindow(panel.getTargetFrame(), this);
        apps.tests.Log4JFixture.tearDown();
    }

	// static private Logger log = LoggerFactory.getLogger(IndicatorTurnoutIconTest.class.getName());
}
