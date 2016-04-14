package jmri.jmrit.display;

import javax.swing.JButton;
import javax.swing.JFrame;
import jmri.jmrix.rps.Measurement;
import jmri.jmrix.rps.Reading;
import jmri.util.JmriJFrame;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for the RpsIcon class.
 *
 * @author	Bob Jacobsen Copyright 2008
 * @version	$Revision$
 */
public class RpsPositionIconTest extends jmri.util.SwingTestCase {

    RpsPositionIcon rpsIcon;
    jmri.jmrit.display.panelEditor.PanelEditor panel;

    public void testShow() {
        JmriJFrame jf = new JmriJFrame("RpsPositionIcon Test");
        jf.getContentPane().setLayout(new java.awt.FlowLayout());

        rpsIcon = new RpsPositionIcon(panel);
        jf.getContentPane().add(rpsIcon);

        jmri.InstanceManager i = new jmri.InstanceManager() {
            protected void init() {
                super.init();
                root = this;
                store(new jmri.managers.InternalTurnoutManager(), jmri.TurnoutManager.class);
            }
        };
        Assert.assertNotNull("Instance exists", i);

        // test buttons
        JButton originButton = new JButton("Set 0,0");
        originButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                measButtonPushed(0., 0.);
            }
        });
        jf.getContentPane().add(originButton);

        JButton tentenButton = new JButton("Set 10,10");
        tentenButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                measButtonPushed(10., 10.);
            }
        });
        jf.getContentPane().add(tentenButton);

        JButton fivetenButton = new JButton("Set 5,10");
        fivetenButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                measButtonPushed(5., 10.);
            }
        });
        jf.getContentPane().add(fivetenButton);

        JButton loco21Button = new JButton("Loco 21");
        loco21Button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                locoButtonPushed("21");
            }
        });
        jf.getContentPane().add(loco21Button);

        JButton loco33Button = new JButton("Loco 33");
        loco33Button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                locoButtonPushed("33");
            }
        });
        jf.getContentPane().add(loco33Button);

        jf.pack();
        jf.setSize(300, 300);
        jf.setVisible(true);

        JFrame f = jmri.util.JmriJFrame.getFrame("RpsPositionIcon Test");
        Assert.assertTrue("found frame", f != null);
        f.dispose();
    }

    String id = "20";

    // animate the visible frame
    public void measButtonPushed(double x, double y) {
        Reading loco = new Reading(id, null);
        Measurement m = new Measurement(loco, x, y, 0.0, 0.133, 0, "source");
        rpsIcon.notify(m);
    }

    public void locoButtonPushed(String newID) {
        id = newID;
    }

//    test order isn't guaranteed!
//    public void testXFrameCreation() {
//        JFrame f = jmri.util.JmriJFrame.getFrame("RpsPositionIcon Test");
//        Assert.assertTrue("found frame", f != null);
//        f.dispose();
//    }

    // from here down is testing infrastructure
    public RpsPositionIconTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", RpsPositionIconTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(RpsPositionIconTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
        
        panel = new jmri.jmrit.display.panelEditor.PanelEditor("Test RpsPositionIcon Panel");
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

	// static private Logger log = LoggerFactory.getLogger(RpsPositionIconTest.class.getName());
}
