package jmri.jmrit.display;

import javax.swing.JFrame;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test the ReporterIcon.
 * <P>
 * There is no default (or internal) implementation, so test via the specific
 * LocoNet implementation
 *
 * Description:
 *
 * @author	Bob Jacobsen Copyright 2007
 * @version	$Revision$
 */
public class ReporterIconTest extends jmri.util.SwingTestCase {

    ReporterIcon to = null;
    jmri.jmrit.display.panelEditor.PanelEditor panel;

    public void testShowSysName() {
        JFrame jf = new JFrame();
        jf.getContentPane().setLayout(new java.awt.FlowLayout());

        to = new ReporterIcon(panel);
        jf.getContentPane().add(to);

        // reset instance manager
        jmri.InstanceManager i = new jmri.InstanceManager() {
            protected void init() {
                super.init();
                root = this;
            }
        };
        Assert.assertNotNull("Instance exists", i);
        // reset the LocoNet instances, so this behaves independent of 
        // any layout connection
        jmri.jmrix.loconet.LocoNetInterfaceScaffold tc = new jmri.jmrix.loconet.LocoNetInterfaceScaffold();

        // create objects to test
        jmri.InstanceManager.setReporterManager(new jmri.jmrix.loconet.LnReporterManager(tc, "L"));
        jmri.InstanceManager.reporterManagerInstance().provideReporter("LR1");
        to.setReporter("LR1");
        jmri.InstanceManager.reporterManagerInstance().provideReporter("LR1").setReport("data");

        jf.pack();
        jf.setVisible(true);

    }

    public void testShowNumericAddress() {
        JFrame jf = new JFrame();
        jf.getContentPane().setLayout(new java.awt.FlowLayout());

        to = new ReporterIcon(panel);
        jf.getContentPane().add(to);

        // reset instance manager
        jmri.InstanceManager i = new jmri.InstanceManager() {
            protected void init() {
                super.init();
                root = this;
            }
        };
        Assert.assertNotNull("Instance exists", i);
        // reset the LocoNet instances, so this behaves independent of 
        // any layout connection
        jmri.jmrix.loconet.LocoNetInterfaceScaffold tc = new jmri.jmrix.loconet.LocoNetInterfaceScaffold();

        // create objects to test
        jmri.InstanceManager.setReporterManager(new jmri.jmrix.loconet.LnReporterManager(tc, "L"));
        jmri.InstanceManager.reporterManagerInstance().provideReporter("1");
        to.setReporter("1");
        jmri.InstanceManager.reporterManagerInstance().provideReporter("1").setReport("data");

        jf.pack();
        jf.setVisible(true);

    }

    // from here down is testing infrastructure
    public ReporterIconTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", ReporterIconTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(ReporterIconTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();

        panel = new jmri.jmrit.display.panelEditor.PanelEditor("Test ReporterIcon Panel");
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
