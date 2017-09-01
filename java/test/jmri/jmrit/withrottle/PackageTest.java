package jmri.jmrit.withrottle;

import jmri.util.JUnitUtil;
import junit.framework.JUnit4TestAdapter;
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
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrit.withrottle.PackageTest");   // no tests in this class itself

        suite.addTest(new junit.framework.JUnit4TestAdapter(BundleTest.class));
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
        suite.addTest(new JUnit4TestAdapter(WiFiConsistTest.class));
        suite.addTest(new JUnit4TestAdapter(WiFiConsistManagerTest.class));
        suite.addTest(WiThrottleManagerTest.suite());
        suite.addTest(WiThrottlePreferencesTest.suite());
        suite.addTest(WiThrottlesListModelTest.suite());
        suite.addTest(new JUnit4TestAdapter(WiThrottlePrefsPanelTest.class));
        suite.addTest(new JUnit4TestAdapter(ControllerFilterActionTest.class));
        suite.addTest(new JUnit4TestAdapter(ControllerFilterFrameTest.class));
        suite.addTest(new JUnit4TestAdapter(UserInterfaceTest.class));
        suite.addTest(new JUnit4TestAdapter(WiThrottleCreationActionTest.class));

        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() {
        JUnitUtil.setUp();
    }

    @Override
    protected void tearDown() {
        JUnitUtil.tearDown();
    }
}
