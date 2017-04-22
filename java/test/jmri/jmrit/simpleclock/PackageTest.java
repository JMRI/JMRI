package jmri.jmrit.simpleclock;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmrit.simpleclock package
 *
 * @author	Bob Jacobsen
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
        TestSuite suite = new TestSuite("jmri.jmrit.simpleclock.PackageTest"); // no tests in class itself
        suite.addTest(jmri.jmrit.simpleclock.SimpleTimebaseTest.suite());
        suite.addTest(new junit.framework.JUnit4TestAdapter(BundleTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrit.simpleclock.configurexml.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(SimpleClockActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(SimpleClockFrameTest.class));
        return suite;
    }

}
