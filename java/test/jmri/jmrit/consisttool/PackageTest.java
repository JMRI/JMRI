package jmri.jmrit.consisttool;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Invokes complete set of tests in the jmri.jmrit.consisttool tree
 *
 * @author	Paul Bender Copyright (C) 2015
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
        TestSuite suite = new TestSuite("jmri.jmrit.consisttool.PackageTest");   // no tests in this class itself

        if (!System.getProperty("jmri.headlesstest", "false").equals("true")) {
            suite.addTest(ConsistToolFrameTest.suite());
            suite.addTest(ConsistDataModelTest.suite());
        }
        suite.addTest(ConsistFileTest.suite());

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
