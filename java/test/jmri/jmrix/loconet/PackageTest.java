package jmri.jmrix.loconet;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.loconet package.
 *
 * @author	Bob Jacobsen Copyright 2001, 2003
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
        TestSuite suite = new TestSuite("jmri.jmrix.loconet.PackageTest");  // no tests in this class itself

        suite.addTest(jmri.jmrix.loconet.LocoNetThrottledTransmitterTest.suite());

        if (!System.getProperty("jmri.headlesstest", "false").equals("true")) {
            suite.addTest(jmri.jmrix.loconet.locostats.PackageTest.suite());
        }

        suite.addTest(jmri.jmrix.loconet.sdf.PackageTest.suite());
        suite.addTest(jmri.jmrix.loconet.locomon.PackageTest.suite());
        suite.addTest(jmri.jmrix.loconet.soundloader.PackageTest.suite());
        suite.addTest(jmri.jmrix.loconet.spjfile.PackageTest.suite());
        suite.addTest(new TestSuite(SlotManagerTest.class));
        suite.addTest(new TestSuite(LocoNetSlotTest.class));
        suite.addTest(new TestSuite(LnOpsModeProgrammerTest.class));
        suite.addTest(new TestSuite(LocoNetMessageTest.class));
        suite.addTest(new TestSuite(LnTrafficControllerTest.class));
        suite.addTest(new TestSuite(LnTrafficRouterTest.class));
        suite.addTest(new TestSuite(LnPacketizerTest.class));
        suite.addTest(new TestSuite(LocoNetThrottleTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(LocoNetConsistTest.class));
        suite.addTest(LnPowerManagerTest.suite());
        suite.addTest(LnTurnoutTest.suite());
        suite.addTest(LnTurnoutManagerTest.suite());
        suite.addTest(LnReporterTest.suite());
        suite.addTest(LnSensorTest.suite());
        suite.addTest(LnSensorAddressTest.suite());
        suite.addTest(LnSensorManagerTest.suite());
        suite.addTest(LnCommandStationTypeTest.suite());
        suite.addTest(BundleTest.suite());
        suite.addTest(jmri.jmrix.loconet.pr3.PackageTest.suite());
        suite.addTest(jmri.jmrix.loconet.hexfile.PackageTest.suite());
        suite.addTest(jmri.jmrix.loconet.lnsvf2.PackageTest.suite());
        suite.addTest(jmri.jmrix.loconet.downloader.PackageTest.suite());

        if (!System.getProperty("jmri.headlesstest", "false").equals("true")) {
            suite.addTest(jmri.jmrix.loconet.locoio.PackageTest.suite());
            suite.addTest(jmri.jmrix.loconet.locogen.PackageTest.suite());
        }

        return suite;
    }

}
