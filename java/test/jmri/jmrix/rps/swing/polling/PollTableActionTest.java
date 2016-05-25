package jmri.jmrix.rps.swing.polling;

import javax.swing.JFrame;
import jmri.util.JUnitUtil;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.rps.swing.polling package.
 *
 * @author Bob Jacobsen Copyright 2008
 */
public class PollTableActionTest extends TestCase {

    // Show the window
    public void testDisplay() {
        new PollTableAction().actionPerformed(null);
        // confirm window was created
        JFrame f = jmri.util.JmriJFrame.getFrame("RPS Polling Control");
        Assert.assertTrue("found frame", f != null);
        f.dispose();
    }

    // from here down is testing infrastructure
    public PollTableActionTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {PollTableActionTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite(PollTableActionTest.class);
        return suite;
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

}
