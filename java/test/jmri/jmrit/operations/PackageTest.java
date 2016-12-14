package jmri.jmrit.operations;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmrit.operations package
 *
 * @author	Bob Coleman
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
        TestSuite suite = new TestSuite("jmri.jmrit.operations.PackageTest"); // no tests in class itself
        suite.addTest(jmri.jmrit.operations.setup.PackageTest.suite());
        suite.addTest(jmri.jmrit.operations.locations.PackageTest.suite()); // fixed references to Swing, 10/10/2012
        suite.addTest(jmri.jmrit.operations.rollingstock.PackageTest.suite());
        suite.addTest(jmri.jmrit.operations.routes.PackageTest.suite());
        suite.addTest(jmri.jmrit.operations.trains.PackageTest.suite());  // fixed references to Swing, 10/10/2012
        suite.addTest(jmri.jmrit.operations.router.PackageTest.suite());  // fixed references to Swing, 10/10/2012
        suite.addTest(jmri.jmrit.operations.automation.PackageTest.suite());

        suite.addTest(XmlLoadTests.suite()); // no tests in class itself
        suite.addTest(new junit.framework.JUnit4TestAdapter(BundleTest.class)); 
        return suite;
    }

}
