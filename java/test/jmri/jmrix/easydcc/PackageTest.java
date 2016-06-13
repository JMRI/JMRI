package jmri.jmrix.easydcc;

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

        suite.addTest(jmri.jmrix.easydcc.EasyDccTurnoutTest.suite());
        suite.addTest(jmri.jmrix.easydcc.EasyDccTurnoutManagerTest.suite());
        suite.addTest(jmri.jmrix.easydcc.EasyDccProgrammerTest.suite());
        suite.addTest(jmri.jmrix.easydcc.EasyDccTrafficControllerTest.suite());
        suite.addTest(jmri.jmrix.easydcc.EasyDccMessageTest.suite());
        suite.addTest(jmri.jmrix.easydcc.EasyDccReplyTest.suite());
        suite.addTest(jmri.jmrix.easydcc.EasyDccPowerManagerTest.suite());
        suite.addTest(jmri.jmrix.easydcc.EasyDccConsistManagerTest.suite());
        suite.addTest(jmri.jmrix.easydcc.EasyDccConsistTest.suite());
        if (!System.getProperty("jmri.headlesstest", "false").equals("true")) {
            suite.addTest(jmri.jmrix.easydcc.easydccmon.EasyDccMonFrameTest.suite());
            suite.addTest(jmri.jmrix.easydcc.easydccmon.EasyDccMonActionTest.suite());
            suite.addTest(jmri.jmrix.easydcc.packetgen.EasyDccPacketGenFrameTest.suite());
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
