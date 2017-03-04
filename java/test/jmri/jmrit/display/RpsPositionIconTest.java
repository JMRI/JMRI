package jmri.jmrit.display;

import java.awt.GraphicsEnvironment;
import java.awt.event.WindowListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import jmri.jmrix.rps.Measurement;
import jmri.jmrix.rps.Reading;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * Tests for the RpsIcon class.
 *
 * @author	Bob Jacobsen Copyright 2008
 */
public class RpsPositionIconTest extends jmri.util.SwingTestCase {

    jmri.jmrit.display.panelEditor.PanelEditor panel = null;

    public void testShow() {
        if (GraphicsEnvironment.isHeadless()) {
            return; // can't Assume in TestCase
        }
        JmriJFrame jf = new JmriJFrame("RpsPositionIcon Test");
        jf.getContentPane().setLayout(new java.awt.FlowLayout());

        RpsPositionIcon rpsIcon = new RpsPositionIcon(panel);
        jf.getContentPane().add(rpsIcon);

        // test buttons
        JButton originButton = new JButton("Set 0,0");
        originButton.addActionListener((java.awt.event.ActionEvent e) -> {
            measButtonPushed(0., 0., rpsIcon);
        });
        jf.getContentPane().add(originButton);

        JButton tentenButton = new JButton("Set 10,10");
        tentenButton.addActionListener((java.awt.event.ActionEvent e) -> {
            measButtonPushed(10., 10., rpsIcon);
        });
        jf.getContentPane().add(tentenButton);

        JButton fivetenButton = new JButton("Set 5,10");
        fivetenButton.addActionListener((java.awt.event.ActionEvent e) -> {
            measButtonPushed(5., 10., rpsIcon);
        });
        jf.getContentPane().add(fivetenButton);

        JButton loco21Button = new JButton("Loco 21");
        loco21Button.addActionListener((java.awt.event.ActionEvent e) -> {
            locoButtonPushed("21");
        });
        jf.getContentPane().add(loco21Button);

        JButton loco33Button = new JButton("Loco 33");
        loco33Button.addActionListener((java.awt.event.ActionEvent e) -> {
            locoButtonPushed("33");
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
    public void measButtonPushed(double x, double y, RpsPositionIcon rpsIcon) {
        Reading loco = new Reading(id, null);
        Measurement m = new Measurement(loco, x, y, 0.0, 0.133, 0, "source");
        rpsIcon.notify(m);
    }

    public void locoButtonPushed(String newID) {
        id = newID;
    }

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
    @Override
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initDefaultUserMessagePreferences();
        if (!GraphicsEnvironment.isHeadless()) {
            panel = new jmri.jmrit.display.panelEditor.PanelEditor("Test RpsPositionIcon Panel");
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
        JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(RpsPositionIconTest.class.getName());
}
