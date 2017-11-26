package jmri.jmrit.display;

import java.awt.GraphicsEnvironment;
import java.awt.event.WindowListener;
import javax.swing.JFrame;
import jmri.jmrit.display.panelEditor.PanelEditor;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * IndicatorTurnoutIconTest.java
 *
 * @author Bob Jacobsen
 */
public class IndicatorTurnoutIconTest {

    PanelEditor panel = null;

    @Test
    public void testEquals() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
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

    @Test
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

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();        if (!GraphicsEnvironment.isHeadless()) {
            panel = new PanelEditor("Test IndicatorTurnoutIcon Panel");
        }
    }

    @After
    public void tearDown() {
        // now close panel window
        if (panel != null) {
            java.awt.event.WindowListener[] listeners = panel.getTargetFrame().getWindowListeners();
            for (WindowListener listener : listeners) {
                panel.getTargetFrame().removeWindowListener(listener);
            }
            panel.getTargetFrame().dispose();
            JUnitUtil.dispose(panel);
        }
        apps.tests.Log4JFixture.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(IndicatorTurnoutIconTest.class);
}
