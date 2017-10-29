package jmri.jmrix.dccpp;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.dccpp package
 *
 * @author	Bob Jacobsen
 * @author	Mark Underwood
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
        TestSuite suite = new TestSuite("jmri.jmrix.dccpp.DCCppTest");  // no tests in this class itself
	suite.addTest(new TestSuite(DCCppCommandStationTest.class));
        suite.addTest(new TestSuite(DCCppConnectionTypeListTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(DCCppMessageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(DCCppReplyTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(DCCppPacketizerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(DCCppTrafficControllerTest.class));
        suite.addTest(new TestSuite(DCCppSystemConnectionMemoTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(DCCppThrottleTest.class));
        suite.addTest(new TestSuite(DCCppInitializationManagerTest.class));
        suite.addTest(new TestSuite(DCCppProgrammerTest.class));
        suite.addTest(new TestSuite(DCCppProgrammerManagerTest.class));
        suite.addTest(new TestSuite(DCCppPowerManagerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(DCCppThrottleManagerTest.class));
        suite.addTest(new TestSuite(DCCppLightTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(DCCppLightManagerTest.class));
        suite.addTest(new TestSuite(DCCppOpsModeProgrammerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(DCCppStreamPortControllerTest.class));
        suite.addTest(new TestSuite(DCCppSensorTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(DCCppSensorManagerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(DCCppTurnoutTest.class));
        suite.addTest(jmri.jmrix.dccpp.network.PackageTest.suite());
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.dccpp.swing.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.dccpp.dccppovertcp.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.dccpp.simulator.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.dccpp.serial.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.dccpp.configurexml.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(DCCppNetworkPortControllerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(DCCppSerialPortControllerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(DCCppSimulatorPortControllerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(DCCppMessageExceptionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(DCCppConstantsTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(DCCppRegisterManagerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(DCCppMultiMeterTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(DCCppTurnoutManagerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(DCCppTurnoutReplyCacheTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(BundleTest.class));
        return suite;
    }

}
