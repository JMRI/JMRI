package jmri.jmrit.withrottle;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Invokes complete set of tests in the jmri.jmrit.withrottle tree
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
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrit.withrottle.PackageTest");   // no tests in this class itself

        suite.addTest(BundleTest.suite());
        suite.addTest(ConsistControllerTest.suite());
        suite.addTest(ConsistFunctionControllerTest.suite());
        suite.addTest(DeviceServerTest.suite());
        suite.addTest(FacelessServerTest.suite());
        suite.addTest(MultiThrottleControllerTest.suite());
        suite.addTest(MultiThrottleTest.suite());
        suite.addTest(RouteControllerTest.suite());
        suite.addTest(ThrottleControllerTest.suite());
        suite.addTest(TrackPowerControllerTest.suite());
        suite.addTest(TurnoutControllerTest.suite());
        suite.addTest(WiFiConsistFileTest.suite());
        suite.addTest(WiFiConsistTest.suite());
        suite.addTest(WiFiConsistManagerTest.suite());
        suite.addTest(WiThrottleManagerTest.suite());
        suite.addTest(WiThrottlePreferencesTest.suite());
        suite.addTest(WiThrottlesListModelTest.suite());


        if (!System.getProperty("jmri.headlesstest", "false").equals("true")) {
            suite.addTest(ControllerFilterActionTest.suite());
            suite.addTest(ControllerFilterFrameTest.suite());
            suite.addTest(UserInterfaceTest.suite());
            suite.addTest(WiThrottlePrefsPanelTest.suite());
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
