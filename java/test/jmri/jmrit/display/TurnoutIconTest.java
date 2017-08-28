package jmri.jmrit.display;

import java.awt.GraphicsEnvironment;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import javax.swing.JButton;
import javax.swing.JFrame;
import jmri.util.JUnitUtil;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * TurnoutIconTest.java
 *
 * @author	Bob Jacobsen
 */
public class TurnoutIconTest extends jmri.util.SwingTestCase {

    jmri.jmrit.display.panelEditor.PanelEditor panel = null;

    public void testEquals() {
        if (GraphicsEnvironment.isHeadless()) {
            return; // can't Assume in TestCase
        }
        JFrame jf = new JFrame();
        jf.getContentPane().setLayout(new java.awt.FlowLayout());

        TurnoutIcon to = new TurnoutIcon(panel);
        jmri.Turnout turnout = jmri.InstanceManager.turnoutManagerInstance().provideTurnout("IT1");
        to.setTurnout(new jmri.NamedBeanHandle<>("IT1", turnout));

        TurnoutIcon to2 = new TurnoutIcon(panel);
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

        TurnoutIcon to = new TurnoutIcon(panel);
        jmri.Turnout turnout = jmri.InstanceManager.turnoutManagerInstance().provideTurnout("IT1");
        to.setTurnout(new jmri.NamedBeanHandle<>("IT1", turnout));

        TurnoutIcon to2 = (TurnoutIcon) to.deepClone();

        Assert.assertFalse("clone object (not content) equality", to2.equals(to));

        Assert.assertTrue("class type equality", to2.getClass().equals(to.getClass()));

    }

    public void testShow() {
        if (GraphicsEnvironment.isHeadless()) {
            return; // can't Assume in TestCase
        }
        JFrame jf = new JFrame();
        jf.getContentPane().setLayout(new java.awt.FlowLayout());

        TurnoutIcon to = new TurnoutIcon(panel);
        jf.getContentPane().add(to);

        jmri.Turnout turnout = jmri.InstanceManager.turnoutManagerInstance().provideTurnout("IT1");
        to.setTurnout(new jmri.NamedBeanHandle<>("IT1", turnout));

        // test buttons
        JButton throwButton = new JButton("throw");
        throwButton.addActionListener((java.awt.event.ActionEvent e) -> {
            throwButtonPushed();
        });
        jf.getContentPane().add(throwButton);
        JButton closeButton = new JButton("close");
        closeButton.addActionListener((java.awt.event.ActionEvent e) -> {
            closeButtonPushed();
        });
        jf.getContentPane().add(closeButton);
        JButton unknownButton = new JButton("unknown");
        unknownButton.addActionListener((java.awt.event.ActionEvent e) -> {
            unknownButtonPushed();
        });
        jf.getContentPane().add(unknownButton);
        JButton inconsistentButton = new JButton("inconsistent");
        inconsistentButton.addActionListener((java.awt.event.ActionEvent e) -> {
            inconsistentButtonPushed();
        });
        jf.getContentPane().add(inconsistentButton);

        jf.pack();
        jf.setVisible(true);

    }

    // animate the visible frame
    public void throwButtonPushed() {
        TurnoutIcon to = new TurnoutIcon(panel);
        java.beans.PropertyChangeEvent e = new PropertyChangeEvent(this,
                "KnownState", null, jmri.Turnout.THROWN);
        to.propertyChange(e);
    }

    public void closeButtonPushed() {
        TurnoutIcon to = new TurnoutIcon(panel);
        java.beans.PropertyChangeEvent e = new PropertyChangeEvent(this,
                "KnownState", null, jmri.Turnout.CLOSED);
        to.propertyChange(e);
    }

    public void unknownButtonPushed() {
        TurnoutIcon to = new TurnoutIcon(panel);
        java.beans.PropertyChangeEvent e = new PropertyChangeEvent(this,
                "KnownState", null, jmri.Turnout.UNKNOWN);
        to.propertyChange(e);
    }

    public void inconsistentButtonPushed() {
        TurnoutIcon to = new TurnoutIcon(panel);
        java.beans.PropertyChangeEvent e = new PropertyChangeEvent(this,
                "KnownState", null, 23);
        to.propertyChange(e);
    }

    // from here down is testing infrastructure
    public TurnoutIconTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", TurnoutIconTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(TurnoutIconTest.class);
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() {
        JUnitUtil.setUp();
        if (!GraphicsEnvironment.isHeadless()) {
            panel = new jmri.jmrit.display.panelEditor.PanelEditor("Test TurnoutIcon Panel");
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
            JUnitUtil.resetWindows(false, false);  // don't log here.  should be from this class.
        }
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(TurnoutIconTest.class.getName());
}
