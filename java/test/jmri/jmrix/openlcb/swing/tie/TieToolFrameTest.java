package jmri.jmrix.openlcb.swing.tie;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.can.swing.tie.TieToolFrame class
 *
 * @author Bob Jacobsen Copyright 2008
 */
public class TieToolFrameTest extends TestCase {

    public void testCreateAndShow() throws Exception {

        // skip if headless, as requires display to show
        if (System.getProperty("jmri.headlesstest", "false").equals("true")) {
            return;
        }

        jmri.util.JmriJFrame f = new TieToolFrame();
        f.initComponents();
        f.pack();

        f.setVisible(true);

        // close frame
        f.dispose();
    }

    // from here down is testing infrastructure
    public TieToolFrameTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        apps.tests.AllTest.initLogging();
        String[] testCaseName = {"-noloading", TieToolFrameTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite(TieToolFrameTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }
}
