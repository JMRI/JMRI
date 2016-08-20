package jmri.jmrit.display;

import java.beans.PropertyChangeEvent;
import javax.swing.JButton;
import javax.swing.JFrame;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * TurnoutIconTest.java
 *
 * @author	Bob Jacobsen
 */
public class TurnoutIconTest extends jmri.util.SwingTestCase {

    TurnoutIcon to;
    jmri.jmrit.display.panelEditor.PanelEditor panel;

    public void testEquals() {
        JFrame jf = new JFrame();
        jf.getContentPane().setLayout(new java.awt.FlowLayout());

        to = new TurnoutIcon(panel);
        jmri.Turnout turnout = jmri.InstanceManager.turnoutManagerInstance().provideTurnout("IT1");
        to.setTurnout(new jmri.NamedBeanHandle<jmri.Turnout>("IT1", turnout));

        TurnoutIcon to2 = new TurnoutIcon(panel);
        turnout = jmri.InstanceManager.turnoutManagerInstance().provideTurnout("IT1");
        to2.setTurnout(new jmri.NamedBeanHandle<jmri.Turnout>("IT1", turnout));

        Assert.assertTrue("identity", to.equals(to));
        Assert.assertFalse("object (not content) equality", to2.equals(to));
        Assert.assertFalse("object (not content) equality commutes", to.equals(to2));        
    }

    public void testClone() {
        JFrame jf = new JFrame();
        jf.getContentPane().setLayout(new java.awt.FlowLayout());

        to = new TurnoutIcon(panel);
        jmri.Turnout turnout = jmri.InstanceManager.turnoutManagerInstance().provideTurnout("IT1");
        to.setTurnout(new jmri.NamedBeanHandle<jmri.Turnout>("IT1", turnout));

        TurnoutIcon to2 = (TurnoutIcon) to.deepClone();

        Assert.assertFalse("clone object (not content) equality", to2.equals(to));
        
        Assert.assertTrue("class type equality", to2.getClass().equals(to.getClass()));
        
    }
    
    public void testShow() {
        JFrame jf = new JFrame();
        jf.getContentPane().setLayout(new java.awt.FlowLayout());

        to = new TurnoutIcon(panel);
        jf.getContentPane().add(to);

        jmri.Turnout turnout = jmri.InstanceManager.turnoutManagerInstance().provideTurnout("IT1");
        to.setTurnout(new jmri.NamedBeanHandle<jmri.Turnout>("IT1", turnout));

        // test buttons
        JButton throwButton = new JButton("throw");
        throwButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                throwButtonPushed();
            }
        });
        jf.getContentPane().add(throwButton);
        JButton closeButton = new JButton("close");
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                closeButtonPushed();
            }
        });
        jf.getContentPane().add(closeButton);
        JButton unknownButton = new JButton("unknown");
        unknownButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                unknownButtonPushed();
            }
        });
        jf.getContentPane().add(unknownButton);
        JButton inconsistentButton = new JButton("inconsistent");
        inconsistentButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                inconsistentButtonPushed();
            }
        });
        jf.getContentPane().add(inconsistentButton);

        jf.pack();
        jf.setVisible(true);

    }

    // animate the visible frame
    public void throwButtonPushed() {
        java.beans.PropertyChangeEvent e = new PropertyChangeEvent(this,
                "KnownState", null, Integer.valueOf(jmri.Turnout.THROWN));
        to.propertyChange(e);
    }

    public void closeButtonPushed() {
        java.beans.PropertyChangeEvent e = new PropertyChangeEvent(this,
                "KnownState", null, Integer.valueOf(jmri.Turnout.CLOSED));
        to.propertyChange(e);
    }

    public void unknownButtonPushed() {
        java.beans.PropertyChangeEvent e = new PropertyChangeEvent(this,
                "KnownState", null, Integer.valueOf(jmri.Turnout.UNKNOWN));
        to.propertyChange(e);
    }

    public void inconsistentButtonPushed() {
        java.beans.PropertyChangeEvent e = new PropertyChangeEvent(this,
                "KnownState", null, Integer.valueOf(23));
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
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    
        jmri.util.JUnitUtil.resetInstanceManager();
        panel = new jmri.jmrit.display.panelEditor.PanelEditor("Test TurnoutIcon Panel");
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

	// static private Logger log = LoggerFactory.getLogger(TurnoutIconTest.class.getName());
}
