package jmri.jmrit.signalling;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Invokes complete set of tests in the jmri.jmrit.signalling tree
 *
 * @author	Bob Jacobsen Copyright 2001, 2003, 2012
 */
public class PackageTest extends TestCase {

    // from here down is testing infrastructure
    public PackageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", PackageTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrit.signalling.PackageTest");   // no tests in this class itself

        suite.addTest(BundleTest.suite());
        suite.addTest(jmri.jmrit.signalling.entryexit.PackageTest.suite());
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrit.signalling.configurexml.PackageTest.class));
        if (!System.getProperty("jmri.headlesstest", "false").equals("true")) {
        }

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
