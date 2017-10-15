package jmri.jmrix.srcp;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.srcp package
 *
 * @author	Paul Bender
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
        TestSuite suite = new TestSuite("jmri.jmrix.srcp.SRCPTest");  // no tests in this class itself
        suite.addTest(new junit.framework.JUnit4TestAdapter(SRCPReplyTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(SRCPMessageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(SRCPTrafficControllerTest.class));
        suite.addTest(new TestSuite(SRCPSystemConnectionMemoTest.class));
        suite.addTest(new TestSuite(SRCPBusConnectionMemoTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(SRCPTurnoutManagerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(SRCPTurnoutTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(SRCPSensorManagerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(SRCPSensorTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(SRCPThrottleManagerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(SRCPThrottleTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(SRCPPowerManagerTest.class));
        suite.addTest(new TestSuite(SRCPProgrammerTest.class));
        suite.addTest(new TestSuite(SRCPProgrammerManagerTest.class));
        suite.addTest(new TestSuite(SRCPClockControlTest.class));
        suite.addTest(jmri.jmrix.srcp.parser.PackageTest.suite());
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.srcp.networkdriver.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.srcp.configurexml.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.srcp.swing.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(SRCPPortControllerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(SRCPTrafficControllerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(SRCPConnectionTypeListTest.class));

        return suite;
    }

}
