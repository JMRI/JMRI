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
        if (!System.getProperty("java.awt.headless", "false").equals("true")) {
            suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.can.cbus.swing.PackageTest.class));
        }

        return suite;
    }

}
