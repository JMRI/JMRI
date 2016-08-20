package jmri.profile;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Invoke complete set of tests for the Jmri package
 *
 * @author	Bob Jacobsen, Copyright (C) 2001, 2002, 2007
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
        TestSuite suite = new TestSuite("jmri.profile.PackageTest");  // no tests in this class itself
        suite.addTest(new JUnit4TestAdapter(ProfileTest.class));
        suite.addTest(new TestSuite(ProfileUtilsTest.class));
        suite.addTest(BundleTest.suite());
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    @Override
    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
