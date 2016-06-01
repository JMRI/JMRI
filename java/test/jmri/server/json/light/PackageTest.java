package jmri.server.json.light;

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
        TestSuite suite = new TestSuite("jmri.server.json.light.LightTest");
        suite.addTestSuite(JsonLightHttpServiceTest.class);
        suite.addTestSuite(JsonLightSocketServiceTest.class);
        suite.addTest(BundleTest.suite());

        if (!System.getProperty("jmri.headlesstest", "false").equals("true")) {
            // put any tests that require a UI here.
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
