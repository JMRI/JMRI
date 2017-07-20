package jmri.jmrix.can.cbus;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.can.cbus package.
 *
 * @author Bob Jacobsen Copyright 2008
 */
public class PackageTest extends TestCase {

    // from here down is testing infrastructure
    public PackageTest(String s) {
        super(s);
    }

    public void testDefinitions() {
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", PackageTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite("jmri.jmrix.can.cbus.CbusTest");
        suite.addTest(jmri.jmrix.can.cbus.CbusAddressTest.suite());
        suite.addTest(jmri.jmrix.can.cbus.CbusProgrammerTest.suite());
        suite.addTest(jmri.jmrix.can.cbus.CbusProgrammerManagerTest.suite());
        suite.addTest(new junit.framework.JUnit4TestAdapter(CbusSensorManagerTest.class));
        suite.addTest(jmri.jmrix.can.cbus.CbusSensorTest.suite());
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.can.cbus.configurexml.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.can.cbus.swing.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(CbusReporterManagerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(CbusConstantsTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(CbusEventFilterTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(CbusMessageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(CbusOpCodesTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(CbusCommandStationTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(CbusConfigurationManagerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(CbusDccProgrammerManagerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(CbusDccOpsModeProgrammerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(CbusDccProgrammerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(CbusLightManagerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(CbusLightTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(CbusPowerManagerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(CbusReporterTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(CbusThrottleTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(CbusThrottleManagerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(CbusTurnoutManagerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(CbusTurnoutTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(BundleTest.class));
        return suite;
    }

}
