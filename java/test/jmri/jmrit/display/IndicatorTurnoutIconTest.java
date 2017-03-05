package jmri.jmrit.display;

import java.awt.GraphicsEnvironment;
import java.awt.event.WindowListener;
import javax.swing.JFrame;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * IndicatorTurnoutIconTest.java
 *
 * @author	Bob Jacobsen
 */
public class IndicatorTurnoutIconTest extends jmri.util.SwingTestCase {

    jmri.jmrit.display.panelEditor.PanelEditor panel = null;

    public void testEquals() {
        if (GraphicsEnvironment.isHeadless()) {
            return; // can't Assume in TestCase
        }
        JFrame jf = new JFrame();
        jf.getContentPane().setLayout(new java.awt.FlowLayout());

        IndicatorTurnoutIcon to = new IndicatorTurnoutIcon(panel);
        jmri.Turnout turnout = jmri.InstanceManager.turnoutManagerInstance().provideTurnout("IT1");
        to.setTurnout(new jmri.NamedBeanHandle<>("IT1", turnout));

        IndicatorTurnoutIcon to2 = new IndicatorTurnoutIcon(panel);
        turnout = jmri.InstanceManager.turnoutManagerInstance().provideTurnout("IT1");
        to2.setTurnout(new jmri.NamedBeanHandle<>("IT1", turnout));

        Assert.assertTrue("identity", to.equals(to));
        Assert.assertFalse("object (not content) equality", to2.equals(to));
        Assert.assertFalse("object (not content) equality commutes", to.equals(to2));
    }

    public void testClone() {
        if (GraphicsEnvironment.isHeadless()) {
            return; // can't Assume in TestCase
        }
        JFrame jf = new JFrame();
        jf.getContentPane().setLayout(new java.awt.FlowLayout());

        IndicatorTurnoutIcon to = new IndicatorTurnoutIcon(panel);
        jmri.Turnout turnout = jmri.InstanceManager.turnoutManagerInstance().provideTurnout("IT1");
        to.setTurnout(new jmri.NamedBeanHandle<>("IT1", turnout));

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
    @Override
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();

        jmri.util.JUnitUtil.resetInstanceManager();
        if (!GraphicsEnvironment.isHeadless()) {
            panel = new jmri.jmrit.display.panelEditor.PanelEditor("Test IndicatorTurnoutIcon Panel");
        }
    }

    @Override
    protected void tearDown() {
        // now close panel window
        if (panel != null) {
            java.awt.event.WindowListener[] listeners = panel.getTargetFrame().getWindowListeners();
            for (WindowListener listener : listeners) {
                panel.getTargetFrame().removeWindowListener(listener);
            }
            junit.extensions.jfcunit.TestHelper.disposeWindow(panel.getTargetFrame(), this);
        }
        apps.tests.Log4JFixture.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(IndicatorTurnoutIconTest.class.getName());
}
