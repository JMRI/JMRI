package jmri.jmrix.nce;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * tests for the jmri.jmrix.nce package
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
        String[] testCaseName = {PackageTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite("jmri.jmrix.nce.PackageTest");
        suite.addTest(new junit.framework.JUnit4TestAdapter(NceTurnoutTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(NceSensorManagerTest.class));
        suite.addTest(jmri.jmrix.nce.NceAIUTest.suite());
        suite.addTest(jmri.jmrix.nce.NceProgrammerTest.suite());
        suite.addTest(jmri.jmrix.nce.NceProgrammerManagerTest.suite());
        suite.addTest(new junit.framework.JUnit4TestAdapter(NceTrafficControllerTest.class));
        suite.addTest(jmri.jmrix.nce.NceSystemConnectionMemoTest.suite());
        suite.addTest(jmri.jmrix.nce.NceMessageTest.suite());
        suite.addTest(jmri.jmrix.nce.NceReplyTest.suite());
        suite.addTest(new junit.framework.JUnit4TestAdapter(NcePowerManagerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(BundleTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.nce.clockmon.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(NceConsistTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.nce.networkdriver.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.nce.usbdriver.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.nce.serialdriver.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.nce.simulator.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.nce.configurexml.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.nce.boosterprog.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.nce.cab.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.nce.macro.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.nce.usbinterface.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.nce.ncemon.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.nce.packetgen.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(NceNetworkPortControllerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(NcePortControllerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.nce.swing.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.nce.consist.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(NceBinaryCommandTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(NceCmdStationMemoryTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(NceConnectionTypeListTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(NceMessageCheckTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(NceUSBTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(NceAIUCheckerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(NceClockControlTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(NceConnectionStatusTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(NceConsistManagerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(NceLightManagerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(NceLightTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(NceMenuTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(NceOpsModeProgrammerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(NceSensorTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(NceThrottleManagerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(NceThrottleTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(NceTurnoutMonitorTest.class));


        return suite;
    }

}
