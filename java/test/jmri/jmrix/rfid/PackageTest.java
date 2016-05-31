package jmri.jmrix.rfid;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * tests for the jmri.jmrix.rfid package
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
        TestSuite suite = new TestSuite("jmri.jmrix.rfid.PackageTest");

        suite.addTest(jmri.jmrix.rfid.RfidStreamPortControllerTest.suite());
        suite.addTest(jmri.jmrix.rfid.RfidSystemConnectionMemoTest.suite());
        suite.addTest(jmri.jmrix.rfid.RfidSensorTest.suite());
        suite.addTest(jmri.jmrix.rfid.RfidReporterTest.suite());
        suite.addTest(jmri.jmrix.rfid.protocol.coreid.CoreIdRfidProtocolTest.suite());
        suite.addTest(jmri.jmrix.rfid.protocol.em18.Em18RfidProtocolTest.suite());
        suite.addTest(jmri.jmrix.rfid.protocol.olimex.OlimexRfidProtocolTest.suite());
        suite.addTest(jmri.jmrix.rfid.protocol.parallax.ParallaxRfidProtocolTest.suite());
        suite.addTest(jmri.jmrix.rfid.protocol.seeedstudio.SeeedStudioRfidProtocolTest.suite());
        suite.addTest(ActiveFlagTest.suite());
        return suite;
    }

}
