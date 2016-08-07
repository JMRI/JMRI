package jmri.jmrix.roco.z21;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.roco.z21 package
 *
 * @author Paul Bender
 */
public class Z21Test extends TestCase {

    // from here down is testing infrastructure
    public Z21Test(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading",Z21Test.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrix.roco.z21.z21Test");  // no tests in this class itself
        suite.addTest(new TestSuite(Z21AdapterTest.class));
        suite.addTest(new TestSuite(Z21MessageTest.class));
        suite.addTest(new TestSuite(Z21ReplyTest.class));
        suite.addTest(new TestSuite(Z21TrafficControllerTest.class));
        suite.addTest(new TestSuite(Z21SystemConnectionMemoTest.class));
        suite.addTest(new TestSuite(Z21XPressNetTunnelTest.class));
        suite.addTest(new TestSuite(Z21XNetProgrammerTest.class));
        suite.addTest(new TestSuite(Z21XNetThrottleManagerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(Z21XNetThrottleTest.class));
        suite.addTest(new TestSuite(Z21XNetTurnoutManagerTest.class));
        suite.addTest(new TestSuite(Z21XNetTurnoutTest.class));
        suite.addTest(jmri.jmrix.roco.z21.simulator.Z21SimulatorTest.suite());
        suite.addTest(BundleTest.suite());
        suite.addTest(new junit.framework.JUnit4TestAdapter(ConnectionConfigTest.class));
        return suite;
    }

}
