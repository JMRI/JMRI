package jmri.jmrix.lenz;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.lenz package
 *
 * @author	Bob Jacobsen
 * @version	$Revision$
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
        TestSuite suite = new TestSuite("jmri.jmrix.lenz.XNetTest");  // no tests in this class itself
        suite.addTest(new TestSuite(LenzCommandStationTest.class));
        suite.addTest(new TestSuite(LenzConnectionTypeListTest.class));
        suite.addTest(new TestSuite(XNetMessageTest.class));
        suite.addTest(new TestSuite(XNetReplyTest.class));
        suite.addTest(new TestSuite(XNetTurnoutTest.class));
        suite.addTest(new TestSuite(XNetSensorTest.class));
        suite.addTest(new TestSuite(XNetLightTest.class));
        suite.addTest(new TestSuite(XNetPacketizerTest.class));
        suite.addTest(new TestSuite(XNetTurnoutManagerTest.class));
        suite.addTest(new TestSuite(XNetSensorManagerTest.class));
        suite.addTest(new TestSuite(XNetLightManagerTest.class));
        suite.addTest(new TestSuite(XNetTrafficControllerTest.class));
        suite.addTest(new TestSuite(XNetTrafficRouterTest.class));
        suite.addTest(new TestSuite(XNetSystemConnectionMemoTest.class));
        suite.addTest(new TestSuite(XNetThrottleTest.class));
        suite.addTest(new TestSuite(XNetConsistManagerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(XNetConsistTest.class));
        suite.addTest(new TestSuite(XNetInitializationManagerTest.class));
        suite.addTest(new TestSuite(XNetProgrammerTest.class));
        suite.addTest(new TestSuite(XNetProgrammerManagerTest.class));
        suite.addTest(new TestSuite(XNetOpsModeProgrammerTest.class));
        suite.addTest(new TestSuite(XNetPowerManagerTest.class));
        suite.addTest(new TestSuite(XNetThrottleManagerTest.class));
        suite.addTest(new TestSuite(XNetExceptionTest.class));
        suite.addTest(new TestSuite(XNetMessageExceptionTest.class));
        suite.addTest(new TestSuite(XNetStreamPortControllerTest.class));
        suite.addTest(jmri.jmrix.lenz.li100.LI100Test.suite());
        suite.addTest(jmri.jmrix.lenz.li100f.LI100FTest.suite());
        suite.addTest(jmri.jmrix.lenz.li101.LI101Test.suite());
        suite.addTest(jmri.jmrix.lenz.liusb.LIUSBTest.suite());
        suite.addTest(jmri.jmrix.lenz.xntcp.XnTcpTest.suite());
        suite.addTest(jmri.jmrix.lenz.liusbserver.LIUSBServerTest.suite());
        suite.addTest(jmri.jmrix.lenz.liusbethernet.LIUSBEthernetTest.suite());
        suite.addTest(jmri.jmrix.lenz.xnetsimulator.XNetSimulatorTest.suite());
        suite.addTest(jmri.jmrix.lenz.hornbyelite.EliteTest.suite());
        suite.addTest(BundleTest.suite());

        suite.addTest(jmri.jmrix.lenz.swing.SwingTest.suite());

        if (!System.getProperty("jmri.headlesstest", "false").equals("true")) {
        }

        return suite;
    }

}
