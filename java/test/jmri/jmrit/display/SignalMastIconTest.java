package jmri.jmrit.display;

import java.awt.GraphicsEnvironment;
import java.awt.event.WindowListener;
import javax.swing.JFrame;
import javax.swing.JLabel;
import jmri.InstanceManager;
import jmri.SignalMast;
import jmri.implementation.DefaultSignalHead;
import jmri.jmrit.display.panelEditor.PanelEditor;
import jmri.util.JUnitUtil;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test the SignalMastIcon.
 * <p>
 * Description:
 *
 * @author	Bob Jacobsen Copyright 2009
 */
public class SignalMastIconTest extends jmri.util.SwingTestCase {

    PanelEditor panel = null;

    public void testShowText() {
        if (GraphicsEnvironment.isHeadless()) {
            return; // can't Assume in TestCase
        }
        // this one is for Layout editor, which for now
        // is still in text form.
        JFrame jf = new JFrame("SignalMast Icon Text Test");
        jf.getContentPane().setLayout(new java.awt.FlowLayout());

        SignalMastIcon to = new SignalMastIcon(panel);
        to.setShowAutoText(true);

        jf.getContentPane().add(new JLabel("Should say Approach: "));
        jf.getContentPane().add(to);

        // reset instance manager & create test heads
        InstanceManager.getDefault(jmri.SignalHeadManager.class).register(
                new DefaultSignalHead("IH1") {
            @Override
            protected void updateOutput() {
            }
        }
        );
        InstanceManager.getDefault(jmri.SignalHeadManager.class).register(
                new DefaultSignalHead("IH2") {
            @Override
            protected void updateOutput() {
            }
        }
        );
        InstanceManager.getDefault(jmri.SignalHeadManager.class).register(
                new DefaultSignalHead("IH3") {
            @Override
            protected void updateOutput() {
            }
        }
        );

        SignalMast s = InstanceManager.getDefault(jmri.SignalMastManager.class)
                .provideSignalMast("IF$shsm:basic:one-searchlight:IH1");

        to.setSignalMast(s.getSystemName());

        s.setAspect("Clear");
        s.setAspect("Approach");

        jf.pack();
        jf.setVisible(true);

        // close
        JUnitUtil.dispose(jf);

    }

    public void testShowIcon() {
        if (GraphicsEnvironment.isHeadless()) {
            return; // can't Assume in TestCase
        }
        JFrame jf = new JFrame("SignalMastIcon Icon Test");
        jf.getContentPane().setLayout(new java.awt.FlowLayout());

        SignalMastIcon to = new SignalMastIcon(panel);
        to.setShowAutoText(false);

        jf.getContentPane().add(new JLabel("Should be yellow/red: "));
        jf.getContentPane().add(to);

        // reset instance manager & create test heads
        InstanceManager.getDefault(jmri.SignalHeadManager.class).register(
                new DefaultSignalHead("IH1") {
            @Override
            protected void updateOutput() {
            }
        }
        );
        InstanceManager.getDefault(jmri.SignalHeadManager.class).register(
                new DefaultSignalHead("IH2") {
            @Override
            protected void updateOutput() {
            }
        }
        );
        InstanceManager.getDefault(jmri.SignalHeadManager.class).register(
                new DefaultSignalHead("IH3") {
            @Override
            protected void updateOutput() {
            }
        }
        );

        SignalMast s = InstanceManager.getDefault(jmri.SignalMastManager.class)
                .provideSignalMast("IF$shsm:basic:two-searchlight:IH1:IH2");

        s.setAspect("Clear");

        to.setSignalMast(s.getSystemName());

        s.setAspect("Clear");
        s.setAspect("Approach");

        jf.pack();
        jf.setVisible(true);

        // close
        JUnitUtil.dispose(jf);
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
    @Override
    protected void setUp() {
        JUnitUtil.setUp();
        if (!GraphicsEnvironment.isHeadless()) {
            panel = new PanelEditor("Test SignalMastIcon Panel");
        }
    }

    @Override
    protected void tearDown() {
        // now close panel window
        if (panel != null) {
            WindowListener[] listeners = panel.getTargetFrame().getWindowListeners();
            for (WindowListener listener : listeners) {
                panel.getTargetFrame().removeWindowListener(listener);
            }
            panel.getTargetFrame().dispose();
            JUnitUtil.dispose(panel);
        }
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SignalMastIconTest.class);
}
