package jmri.web.servlet;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Invokes complete set of tests in the jmri.web.servlet tree
 *
 * @author	Bob Jacobsen Copyright 2013
 */
public class PackageTest extends TestCase {

    // from here down is testing infrastructure
    public PackageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {PackageTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.web.servlet.PackageTest");   // no tests in this class itself
        suite.addTest(jmri.web.servlet.config.PackageTest.suite());
        suite.addTest(jmri.web.servlet.directory.PackageTest.suite());
        suite.addTest(jmri.web.servlet.frameimage.PackageTest.suite());
        suite.addTest(jmri.web.servlet.home.PackageTest.suite());
        suite.addTest(jmri.web.servlet.json.PackageTest.suite());
        suite.addTest(jmri.web.servlet.operations.PackageTest.suite());
        suite.addTest(jmri.web.servlet.panel.PackageTest.suite());
        suite.addTest(jmri.web.servlet.roster.PackageTest.suite());
        suite.addTest(jmri.web.servlet.simple.PackageTest.suite());
        suite.addTest(BundleTest.suite());
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
