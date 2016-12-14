package jmri.managers;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Invoke complete set of tests for the jmri.managers
 *
 * @author	Bob Jacobsen, Copyright (C) 2009
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
        TestSuite suite = new TestSuite("jmri.managers.ManagersTest");  // no tests in this class itself
        suite.addTest(DefaultConditionalManagerTest.suite());
        suite.addTest(DefaultIdTagManagerTest.suite());
        suite.addTest(DefaultLogixManagerTest.suite());
        suite.addTest(DefaultSignalSystemManagerTest.suite());
        suite.addTest(DefaultUserMessagePreferencesTest.suite()); // no longer used in JMRI - may be used in other applications
        suite.addTest(new junit.framework.JUnit4TestAdapter(InternalLightManagerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(InternalSensorManagerTest.class));
        suite.addTest(ProxyLightManagerTest.suite());
        suite.addTest(ProxySensorManagerTest.suite());
        suite.addTest(ProxyTurnoutManagerTest.suite());
        suite.addTest(JmriUserPreferencesManagerTest.suite());
        suite.addTest(new junit.framework.JUnit4TestAdapter(BundleTest.class));

        suite.addTest(jmri.managers.configurexml.PackageTest.suite());
        suite.addTest(new junit.framework.JUnit4TestAdapter(InternalReporterManagerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(ProxyReporterManagerTest.class));

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
