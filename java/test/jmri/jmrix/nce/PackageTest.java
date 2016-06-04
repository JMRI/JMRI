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
        suite.addTest(jmri.jmrix.nce.NceTurnoutTest.suite());
        suite.addTest(jmri.jmrix.nce.NceTurnoutManagerTest.suite());
        suite.addTest(jmri.jmrix.nce.NceSensorManagerTest.suite());
        suite.addTest(jmri.jmrix.nce.NceAIUTest.suite());
        suite.addTest(jmri.jmrix.nce.NceProgrammerTest.suite());
        suite.addTest(jmri.jmrix.nce.NceProgrammerManagerTest.suite());
        suite.addTest(jmri.jmrix.nce.NceTrafficControllerTest.suite());
        suite.addTest(jmri.jmrix.nce.NceSystemConnectionMemoTest.suite());
        suite.addTest(jmri.jmrix.nce.NceMessageTest.suite());
        suite.addTest(jmri.jmrix.nce.NceReplyTest.suite());
        suite.addTest(jmri.jmrix.nce.NcePowerManagerTest.suite());
        suite.addTest(jmri.jmrix.nce.BundleTest.suite());
        suite.addTest(jmri.jmrix.nce.clockmon.PackageTest.suite());
        if (!System.getProperty("jmri.headlesstest", "false").equals("true")) {
            suite.addTest(jmri.jmrix.nce.ncemon.NceMonPanelTest.suite());
            suite.addTest(jmri.jmrix.nce.packetgen.NcePacketGenPanelTest.suite());
        }

        return suite;
    }

}
