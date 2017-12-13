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
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test the ReporterIcon.
 * <p>
 * Description:
 *
 * @author Bob Jacobsen Copyright 2007
 */
public class ReporterIconTest extends PositionableTestBase {

    private PanelEditor panel;
    private ReporterIcon to = null;

    @Test
    public void testShowSysName() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JFrame jf = new JFrame();
        jf.getContentPane().setLayout(new java.awt.FlowLayout());

        jf.getContentPane().add(to);

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

        jf.getContentPane().add(to);


        jf.pack();
        jf.setVisible(true);
        JUnitUtil.dispose(jf);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initReporterManager();
        if (!GraphicsEnvironment.isHeadless()) {
            panel = new PanelEditor("Test ReporterIcon Panel");
            p = to = new ReporterIcon(panel);

            // create objects to test
            InstanceManager.getDefault(ReporterManager.class).provideReporter("IR1");
            to.setReporter("IR1");
            InstanceManager.getDefault(ReporterManager.class).provideReporter("IR1").setReport("data");
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
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(TurnoutIconTest.class);
}
