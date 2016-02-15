// PackageTest.java
package jmri.jmrix.dccpp;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.dccpp package
 *
 * @author	Bob Jacobsen
 * @author	Mark Underwood
 * @version	$Revision$
 */
public class PackageTest extends TestCase {

    // from here down is testing infrastructure
    public PackageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {PackageTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrix.dccpp.DCCppTest");  // no tests in this class itself
	suite.addTest(new TestSuite(DCCppCommandStationTest.class));
        suite.addTest(new TestSuite(DCCppConnectionTypeListTest.class));
        suite.addTest(new TestSuite(DCCppMessageTest.class));
        suite.addTest(new TestSuite(DCCppReplyTest.class));
        suite.addTest(new TestSuite(DCCppPacketizerTest.class));
        suite.addTest(new TestSuite(DCCppTrafficControllerTest.class));
        suite.addTest(new TestSuite(DCCppSystemConnectionMemoTest.class));
        suite.addTest(new TestSuite(DCCppThrottleTest.class));
        suite.addTest(new TestSuite(DCCppInitializationManagerTest.class));
        suite.addTest(new TestSuite(DCCppProgrammerTest.class));
        suite.addTest(new TestSuite(DCCppProgrammerManagerTest.class));
        suite.addTest(new TestSuite(DCCppPowerManagerTest.class));
        suite.addTest(new TestSuite(DCCppThrottleManagerTest.class));
        suite.addTest(new TestSuite(DCCppLightTest.class));
        suite.addTest(new TestSuite(DCCppLightManagerTest.class));
        suite.addTest(new TestSuite(DCCppOpsModeProgrammerTest.class));
        suite.addTest(new TestSuite(DCCppStreamPortControllerTest.class));
        suite.addTest(new TestSuite(DCCppSensorTest.class));
        suite.addTest(new TestSuite(DCCppSensorManagerTest.class));
        suite.addTest(jmri.jmrix.dccpp.network.DCCppEthernetTest.suite());
	/*
        suite.addTest(new TestSuite(XNetTurnoutTest.class));
        suite.addTest(new TestSuite(XNetTurnoutManagerTest.class));
        suite.addTest(new TestSuite(XNetConsistManagerTest.class));
        suite.addTest(new TestSuite(XNetConsistTest.class));
        suite.addTest(new TestSuite(XNetExceptionTest.class));
        suite.addTest(new TestSuite(XNetMessageExceptionTest.class));
        suite.addTest(jmri.jmrix.lenz.li100.LI100Test.suite());
        suite.addTest(jmri.jmrix.lenz.li100f.LI100FTest.suite());
        suite.addTest(jmri.jmrix.lenz.li101.LI101Test.suite());
        suite.addTest(jmri.jmrix.lenz.liusb.LIUSBTest.suite());
        suite.addTest(jmri.jmrix.lenz.xntcp.XnTcpTest.suite());
        suite.addTest(jmri.jmrix.lenz.liusbserver.LIUSBServerTest.suite());
        suite.addTest(jmri.jmrix.lenz.xnetsimulator.XNetSimulatorTest.suite());
        suite.addTest(jmri.jmrix.lenz.hornbyelite.EliteTest.suite());
        if (!System.getProperty("jmri.headlesstest", "false").equals("true")) {
            suite.addTest(jmri.jmrix.lenz.swing.SwingTest.suite());
        }
	*/

        return suite;
    }

}
