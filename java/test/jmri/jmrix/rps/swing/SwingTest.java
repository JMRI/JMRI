package jmri.jmrix.rps.swing;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.rps.swing package.
 *
 * @author Bob Jacobsen Copyright 2008
 */
public class SwingTest extends TestCase {

    // from here down is testing infrastructure
    public SwingTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {SwingTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite("jmri.jmrix.rps.SwingTest");
        suite.addTest(jmri.jmrix.rps.swing.AffineEntryPanelTest.suite());
        suite.addTest(jmri.jmrix.rps.swing.polling.PackageTest.suite());
        suite.addTest(jmri.jmrix.rps.swing.debugger.DebuggerTest.suite()); // do last to display in front

        suite.addTest(new junit.framework.JUnit4TestAdapter(BundleTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.rps.swing.soundset.PackageTest.class));
        return suite;
    }

}
