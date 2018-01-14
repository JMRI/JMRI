package jmri.jmrit.display;

import java.awt.GraphicsEnvironment;
import javax.swing.JFrame;
import jmri.jmrit.display.panelEditor.PanelEditor;
import jmri.util.JUnitUtil;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * IndicatorTurnoutIconTest.java
 *
 * @author Bob Jacobsen
 */
public class IndicatorTurnoutIconTest extends PositionableIconTest {

    @Test
    public void testEquals() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JFrame jf = new JFrame();
        jf.getContentPane().setLayout(new java.awt.FlowLayout());

        IndicatorTurnoutIcon to = new IndicatorTurnoutIcon(editor);
        jmri.Turnout turnout = jmri.InstanceManager.turnoutManagerInstance().provideTurnout("IT1");
        to.setTurnout(new jmri.NamedBeanHandle<>("IT1", turnout));

        IndicatorTurnoutIcon to2 = new IndicatorTurnoutIcon(editor);
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

        IndicatorTurnoutIcon to = (IndicatorTurnoutIcon)p; 

        IndicatorTurnoutIcon to2 = (IndicatorTurnoutIcon) to.deepClone();

        Assert.assertFalse("clone object (not content) equality", to2.equals(to));

        Assert.assertTrue("class type equality", to2.getClass().equals(to.getClass()));

    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        if (!GraphicsEnvironment.isHeadless()) {
            editor = new PanelEditor("Test IndicatorTurnoutIcon Panel");
            IndicatorTurnoutIcon to = new IndicatorTurnoutIcon(editor);
            jmri.Turnout turnout = jmri.InstanceManager.turnoutManagerInstance().provideTurnout("IT1");
            to.setTurnout(new jmri.NamedBeanHandle<>("IT1", turnout));
            p = to;
        }
    }


    // private final static Logger log = LoggerFactory.getLogger(IndicatorTurnoutIconTest.class);
}
