package jmri.jmrit.display;

import java.awt.GraphicsEnvironment;
import java.awt.event.WindowListener;
import javax.swing.JFrame;
import jmri.InstanceManager;
import jmri.ReporterManager;
import jmri.jmrit.display.panelEditor.PanelEditor;
import jmri.jmrix.loconet.LnReporterManager;
import jmri.jmrix.loconet.LocoNetInterfaceScaffold;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test the ReporterIcon.
 * <P>
 * There is no default (or internal) implementation, so test via the specific
 * LocoNet implementation
 * <p>
 * Description:
 *
 * @author Bob Jacobsen Copyright 2007
 */
public class ReporterIconTest {

    PanelEditor panel;

    @Test
    public void testShowSysName() {
        if (GraphicsEnvironment.isHeadless()) {
            return; // can't Assume in TestCase
        }
        JFrame jf = new JFrame();
        jf.getContentPane().setLayout(new java.awt.FlowLayout());

        ReporterIcon to = new ReporterIcon(panel);
        jf.getContentPane().add(to);

        // reset the LocoNet instances, so this behaves independent of
        // any layout connection
        LocoNetInterfaceScaffold tc = new LocoNetInterfaceScaffold();

        // create objects to test
        InstanceManager.setReporterManager(new LnReporterManager(tc, "L"));
        InstanceManager.getDefault(ReporterManager.class).provideReporter("LR1");
        to.setReporter("LR1");
        InstanceManager.getDefault(ReporterManager.class).provideReporter("LR1").setReport("data");

        jf.pack();
        jf.setVisible(true);
        JUnitUtil.dispose(jf);
    }

    @Test
    public void testShowNumericAddress() {
        if (GraphicsEnvironment.isHeadless()) {
            return; // can't Assume in TestCase
        }
        JFrame jf = new JFrame();
        jf.getContentPane().setLayout(new java.awt.FlowLayout());

        ReporterIcon to = new ReporterIcon(panel);
        jf.getContentPane().add(to);

        // reset the LocoNet instances, so this behaves independent of
        // any layout connection
        LocoNetInterfaceScaffold tc = new LocoNetInterfaceScaffold();

        // create objects to test
        InstanceManager.setReporterManager(new LnReporterManager(tc, "L"));
        InstanceManager.getDefault(ReporterManager.class).provideReporter("1");
        to.setReporter("1");
        InstanceManager.getDefault(ReporterManager.class).provideReporter("1").setReport("data");

        jf.pack();
        jf.setVisible(true);
        JUnitUtil.dispose(jf);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        if (!GraphicsEnvironment.isHeadless()) {
            panel = new PanelEditor("Test ReporterIcon Panel");
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
            ThreadingUtil.runOnGUI(() -> {
                panel.getTargetFrame().dispose();
                JUnitUtil.dispose(panel);
            });
        }
        apps.tests.Log4JFixture.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(TurnoutIconTest.class);
}
