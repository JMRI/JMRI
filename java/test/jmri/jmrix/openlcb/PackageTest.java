package jmri.jmrix.openlcb;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.openlcb package.
 *
 * @author Bob Jacobsen Copyright 2009, 2012, 2015
 */
public class PackageTest extends TestCase {

    public void testDefinitions() {
    }

    // from here down is testing infrastructure
    public PackageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        apps.tests.AllTest.initLogging();
        String[] testCaseName = {"-noloading", PackageTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrix.openlcb.PackageTest");

        suite.addTest(CanConverterTest.suite());
        suite.addTest(OlcbAddressTest.suite());
        suite.addTest(OpenLcbLocoAddressTest.suite());
        suite.addTest(OlcbSensorManagerTest.suite());
        suite.addTest(OlcbProgrammerTest.suite());
        suite.addTest(OlcbProgrammerManagerTest.suite());
        suite.addTest(OlcbSensorTest.suite());
        suite.addTest(OlcbSystemConnectionMemoTest.suite());
        suite.addTest(OlcbTurnoutManagerTest.suite());
        suite.addTest(OlcbTurnoutTest.suite());
        suite.addTest(OlcbThrottleTest.suite());
        suite.addTest(OlcbThrottleManagerTest.suite());
        suite.addTest(BundleTest.suite());

        suite.addTest(jmri.jmrix.openlcb.swing.PackageTest.suite());

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
