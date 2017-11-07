package jmri.jmrix.easydcc;

import jmri.util.JUnitUtil;
import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.easydcc package
 *
 * @author	Bob Jacobsen
 */
public class PackageTest extends TestCase {

    // from here down is testing infrastructure
    public PackageTest(String s) {
        super(s);
    }

    // a dummy test to avoid JUnit warning
    public void testDemo() {
        assertTrue(true);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", PackageTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite("jmri.jmrix.easydcc.EasyDccTest");

        suite.addTest(new junit.framework.JUnit4TestAdapter(EasyDccTurnoutTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(EasyDccTurnoutManagerTest.class));
        suite.addTest(jmri.jmrix.easydcc.EasyDccProgrammerTest.suite());
        suite.addTest(new junit.framework.JUnit4TestAdapter(EasyDccTrafficControllerTest.class));
        suite.addTest(jmri.jmrix.easydcc.EasyDccMessageTest.suite());
        suite.addTest(jmri.jmrix.easydcc.EasyDccReplyTest.suite());
        suite.addTest(new JUnit4TestAdapter(EasyDccPowerManagerTest.class));
        suite.addTest(new JUnit4TestAdapter(EasyDccConsistTest.class));
        suite.addTest(new JUnit4TestAdapter(EasyDccConsistManagerTest.class));
        suite.addTest(new JUnit4TestAdapter(jmri.jmrix.easydcc.serialdriver.PackageTest.class));
        suite.addTest(new JUnit4TestAdapter(jmri.jmrix.easydcc.networkdriver.PackageTest.class));
        suite.addTest(new JUnit4TestAdapter(jmri.jmrix.easydcc.configurexml.PackageTest.class));
        suite.addTest(new JUnit4TestAdapter(jmri.jmrix.easydcc.easydccmon.EasyDccMonFrameTest.class));
        suite.addTest(jmri.jmrix.easydcc.easydccmon.EasyDccMonActionTest.suite());
        suite.addTest(new JUnit4TestAdapter(jmri.jmrix.easydcc.packetgen.PackageTest.class));
        suite.addTest(new JUnit4TestAdapter(EasyDccNetworkPortControllerTest.class));
        suite.addTest(new JUnit4TestAdapter(EasyDccSystemConnectionMemoTest.class));
        suite.addTest(new JUnit4TestAdapter(EasyDccPortControllerTest.class));
        suite.addTest(new JUnit4TestAdapter(EasyDCCMenuTest.class));
        suite.addTest(new JUnit4TestAdapter(EasyDccConnectionTypeListTest.class));
        suite.addTest(new JUnit4TestAdapter(EasyDccCommandStationTest.class));
        suite.addTest(new JUnit4TestAdapter(EasyDccOpsModeProgrammerTest.class));
        suite.addTest(new JUnit4TestAdapter(EasyDccProgrammerManagerTest.class));
        suite.addTest(new JUnit4TestAdapter(EasyDccThrottleManagerTest.class));
        suite.addTest(new JUnit4TestAdapter(EasyDccThrottleTest.class));

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
