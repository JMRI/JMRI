package jmri.jmrit.display;

import javax.swing.JFrame;
import javax.swing.JLabel;
import jmri.InstanceManager;
import jmri.SignalMast;
import jmri.implementation.DefaultSignalHead;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test the SignalMastIcon.
 *
 * Description:
 *
 * @author	Bob Jacobsen Copyright 2009
 * @version	$Revision$
 */
public class SignalMastIconTest extends jmri.util.SwingTestCase {

    SignalMastIcon to = null;
    jmri.jmrit.display.panelEditor.PanelEditor panel;

    public void testShowText() {
        // this one is for Layout editor, which for now
        // is still in text form.
        JFrame jf = new JFrame("SignalMast Icon Text Test");
        jf.getContentPane().setLayout(new java.awt.FlowLayout());

        to = new SignalMastIcon(panel);
        to.setShowAutoText(true);

        jf.getContentPane().add(new JLabel("Should say Approach: "));
        jf.getContentPane().add(to);

        // reset instance manager & create test heads
        jmri.util.JUnitUtil.resetInstanceManager();
        InstanceManager.signalHeadManagerInstance().register(
                new DefaultSignalHead("IH1") {
                    protected void updateOutput() {
                    }
                }
        );
        InstanceManager.signalHeadManagerInstance().register(
                new DefaultSignalHead("IH2") {
                    protected void updateOutput() {
                    }
                }
        );
        InstanceManager.signalHeadManagerInstance().register(
                new DefaultSignalHead("IH3") {
                    protected void updateOutput() {
                    }
                }
        );

        SignalMast s = InstanceManager.signalMastManagerInstance()
                .provideSignalMast("IF$shsm:basic:one-searchlight:IH1");

        to.setSignalMast(s.getSystemName());

        s.setAspect("Clear");
        s.setAspect("Approach");

        jf.pack();
        jf.setVisible(true);

        // close
        jf.dispose();

    }

    public void testShowIcon() {
        JFrame jf = new JFrame("SignalMastIcon Icon Test");
        jf.getContentPane().setLayout(new java.awt.FlowLayout());

        to = new SignalMastIcon(panel);
        to.setShowAutoText(false);

        jf.getContentPane().add(new JLabel("Should be yellow/red: "));
        jf.getContentPane().add(to);

        // reset instance manager & create test heads
        jmri.util.JUnitUtil.resetInstanceManager();
        InstanceManager.signalHeadManagerInstance().register(
                new DefaultSignalHead("IH1") {
                    protected void updateOutput() {
                    }
                }
        );
        InstanceManager.signalHeadManagerInstance().register(
                new DefaultSignalHead("IH2") {
                    protected void updateOutput() {
                    }
                }
        );
        InstanceManager.signalHeadManagerInstance().register(
                new DefaultSignalHead("IH3") {
                    protected void updateOutput() {
                    }
                }
        );

        SignalMast s = InstanceManager.signalMastManagerInstance()
                .provideSignalMast("IF$shsm:basic:two-searchlight:IH1:IH2");

        s.setAspect("Clear");

        to.setSignalMast(s.getSystemName());

        s.setAspect("Clear");
        s.setAspect("Approach");

        jf.pack();
        jf.setVisible(true);

        // close
        jf.dispose();
    }

    // from here down is testing infrastructure
    public SignalMastIconTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", SignalMastIconTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(SignalMastIconTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
        panel = new jmri.jmrit.display.panelEditor.PanelEditor("Test SignalMastIcon Panel");
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

	// static private Logger log = LoggerFactory.getLogger(SignalMastIconTest.class.getName());
}
