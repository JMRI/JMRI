package jmri.jmrix.loconet;

import junit.framework.JUnit4TestAdapter;
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
        suite.addTest(new JUnit4TestAdapter(jmri.jmrix.loconet.locostats.PackageTest.class));
        suite.addTest(jmri.jmrix.loconet.sdf.PackageTest.suite());
        suite.addTest(new JUnit4TestAdapter(jmri.jmrix.loconet.sdfeditor.PackageTest.class));
        suite.addTest(jmri.jmrix.loconet.locomon.PackageTest.suite());
        suite.addTest(jmri.jmrix.loconet.soundloader.PackageTest.suite());
        suite.addTest(new JUnit4TestAdapter(jmri.jmrix.loconet.spjfile.PackageTest.class));
        suite.addTest(new TestSuite(SlotManagerTest.class));
        suite.addTest(new TestSuite(LocoNetSlotTest.class));
        suite.addTest(new TestSuite(LnOpsModeProgrammerTest.class));
        suite.addTest(new TestSuite(LocoNetMessageTest.class));
        suite.addTest(new TestSuite(LnTrafficControllerTest.class));
        suite.addTest(new TestSuite(LnTrafficRouterTest.class));
        suite.addTest(new TestSuite(LnPacketizerTest.class));
        suite.addTest(new JUnit4TestAdapter(LocoNetThrottleTest.class));
        suite.addTest(new JUnit4TestAdapter(LocoNetConsistTest.class));
        suite.addTest(new JUnit4TestAdapter(LnPowerManagerTest.class));
        suite.addTest(new JUnit4TestAdapter(LnTurnoutTest.class));
        suite.addTest(new JUnit4TestAdapter(LnTurnoutManagerTest.class));
        suite.addTest(LnReporterTest.suite());
        suite.addTest(LnSensorTest.suite());
        suite.addTest(LnSensorAddressTest.suite());
        suite.addTest(new JUnit4TestAdapter(LnSensorManagerTest.class));
        suite.addTest(LnCommandStationTypeTest.suite());
        suite.addTest(new JUnit4TestAdapter(BundleTest.class));
        suite.addTest(jmri.jmrix.loconet.pr3.PackageTest.suite());
        suite.addTest(jmri.jmrix.loconet.hexfile.PackageTest.suite());
        suite.addTest(jmri.jmrix.loconet.lnsvf2.PackageTest.suite());
        suite.addTest(jmri.jmrix.loconet.downloader.PackageTest.suite());
        suite.addTest(new JUnit4TestAdapter(jmri.jmrix.loconet.configurexml.PackageTest.class));
        suite.addTest(new JUnit4TestAdapter(jmri.jmrix.loconet.clockmon.PackageTest.class));
        suite.addTest(new JUnit4TestAdapter(jmri.jmrix.loconet.cmdstnconfig.PackageTest.class));
        suite.addTest(new JUnit4TestAdapter(jmri.jmrix.loconet.duplexgroup.PackageTest.class));
        suite.addTest(new JUnit4TestAdapter(jmri.jmrix.loconet.locoid.PackageTest.class));
        suite.addTest(new JUnit4TestAdapter(jmri.jmrix.loconet.slotmon.PackageTest.class));
        suite.addTest(new JUnit4TestAdapter(jmri.jmrix.loconet.swing.PackageTest.class));
        suite.addTest(new JUnit4TestAdapter(jmri.jmrix.loconet.bdl16.PackageTest.class));
        suite.addTest(new JUnit4TestAdapter(jmri.jmrix.loconet.ds64.PackageTest.class));
        suite.addTest(new JUnit4TestAdapter(jmri.jmrix.loconet.se8.PackageTest.class));
        suite.addTest(new JUnit4TestAdapter(jmri.jmrix.loconet.pm4.PackageTest.class));
        suite.addTest(new JUnit4TestAdapter(jmri.jmrix.loconet.Intellibox.PackageTest.class));
        suite.addTest(new JUnit4TestAdapter(jmri.jmrix.loconet.bluetooth.PackageTest.class));
        suite.addTest(new JUnit4TestAdapter(jmri.jmrix.loconet.locobuffer.PackageTest.class));
        suite.addTest(new JUnit4TestAdapter(jmri.jmrix.loconet.locobufferii.PackageTest.class));
        suite.addTest(new JUnit4TestAdapter(jmri.jmrix.loconet.locobufferusb.PackageTest.class));
        suite.addTest(new JUnit4TestAdapter(jmri.jmrix.loconet.loconetovertcp.PackageTest.class));
        suite.addTest(new JUnit4TestAdapter(jmri.jmrix.loconet.ms100.PackageTest.class));
        suite.addTest(new JUnit4TestAdapter(jmri.jmrix.loconet.pr2.PackageTest.class));
        suite.addTest(new JUnit4TestAdapter(jmri.jmrix.loconet.uhlenbrock.PackageTest.class));
        suite.addTest(new JUnit4TestAdapter(jmri.jmrix.loconet.locormi.PackageTest.class));
        suite.addTest(new JUnit4TestAdapter(LnReporterManagerTest.class));
        suite.addTest(jmri.jmrix.loconet.locoio.PackageTest.suite());
        suite.addTest(jmri.jmrix.loconet.locogen.PackageTest.suite());
        suite.addTest(new JUnit4TestAdapter(LnNetworkPortControllerTest.class));
        suite.addTest(new JUnit4TestAdapter(LocoNetSystemConnectionMemoTest.class));
        suite.addTest(new JUnit4TestAdapter(LnPortControllerTest.class));
        suite.addTest(new JUnit4TestAdapter(LocoNetExceptionTest.class));
        suite.addTest(new JUnit4TestAdapter(LocoNetMessageExceptionTest.class));
        suite.addTest(new JUnit4TestAdapter(LnConnectionTypeListTest.class));
        suite.addTest(new JUnit4TestAdapter(LnConstantsTest.class));
        suite.addTest(new JUnit4TestAdapter(Ib1ThrottleManagerTest.class));
        suite.addTest(new JUnit4TestAdapter(Ib1ThrottleTest.class));
        suite.addTest(new JUnit4TestAdapter(Ib2ThrottleManagerTest.class));
        suite.addTest(new JUnit4TestAdapter(Ib2ThrottleTest.class));
        suite.addTest(new JUnit4TestAdapter(LNCPSignalMastTest.class));
        suite.addTest(new JUnit4TestAdapter(LnLightManagerTest.class));
        suite.addTest(new JUnit4TestAdapter(LnLightTest.class));
        suite.addTest(new JUnit4TestAdapter(LnMessageManagerTest.class));
        suite.addTest(new JUnit4TestAdapter(LnPr2ThrottleManagerTest.class));
        suite.addTest(new JUnit4TestAdapter(Pr2ThrottleTest.class));
        suite.addTest(new JUnit4TestAdapter(LnClockControlTest.class));
        suite.addTest(new JUnit4TestAdapter(LnProgrammerManagerTest.class));
        suite.addTest(new JUnit4TestAdapter(LnThrottleManagerTest.class));
        suite.addTest(new JUnit4TestAdapter(LocoNetConsistManagerTest.class));
        suite.addTest(new JUnit4TestAdapter(SE8cSignalHeadTest.class));
        suite.addTest(new JUnit4TestAdapter(UhlenbrockSlotManagerTest.class));
        suite.addTest(new JUnit4TestAdapter(UhlenbrockSlotTest.class));
        return suite;
    }

}
