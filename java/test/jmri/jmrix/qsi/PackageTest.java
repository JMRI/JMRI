package jmri.jmrix.qsi;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.qsi package
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
        String[] testCaseName = {"-noloading", PackageTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite("jmri.jmrix.qsi.QsiTest");
        suite.addTest(jmri.jmrix.qsi.QsiTrafficControllerTest.suite());
        suite.addTest(jmri.jmrix.qsi.QsiMessageTest.suite());
        suite.addTest(jmri.jmrix.qsi.QsiReplyTest.suite());

        if (!System.getProperty("jmri.headlesstest", "false").equals("true")) {
            suite.addTest(jmri.jmrix.qsi.qsimon.QsiMonFrameTest.suite());
            suite.addTest(jmri.jmrix.qsi.packetgen.PacketGenFrameTest.suite());
        }

        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }
}
