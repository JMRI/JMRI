package jmri.server.json;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *
 * @author	Paul Bender Copyright 2010
 * @author Randall Wood (C) 2016
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
        TestSuite suite = new TestSuite("jmri.server.JsonTest");
        suite.addTest(BundleTest.suite());
        suite.addTest(jmri.server.json.light.PackageTest.suite());
        suite.addTest(jmri.server.json.memory.PackageTest.suite());
        suite.addTest(jmri.server.json.power.PackageTest.suite());
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.server.json.roster.PackageTest.class));
        suite.addTest(jmri.server.json.route.PackageTest.suite());
        suite.addTest(jmri.server.json.sensor.PackageTest.suite());
        suite.addTest(jmri.server.json.turnout.PackageTest.suite());
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.server.json.throttle.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.server.json.util.PackageTest.class));

        if (!System.getProperty("jmri.headlesstest", "false").equals("true")) {
            // put any tests that require a UI here.
        }

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
