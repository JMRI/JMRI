// PackageTest.java
package jmri.jmrix.can;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.can package.
 *
 * @author Bob Jacobsen Copyright 2008
 */
public class PackageTest extends TestCase {

    public void testDefinitions() {
    }

    // from here down is testing infrastructure
    public PackageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        apps.tests.AllTest.initLogging();
        String[] testCaseName = {"-noloading", PackageTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrix.can.CanTest");
        suite.addTest(jmri.jmrix.can.CanMessageTest.suite());
        suite.addTest(jmri.jmrix.can.CanReplyTest.suite());
        suite.addTest(jmri.jmrix.can.nmranet.PackageTest.suite());
        suite.addTest(jmri.jmrix.can.adapters.PackageTest.suite());

        if (!System.getProperty("jmri.headlesstest", "false").equals("true")) {
            suite.addTest(jmri.jmrix.can.swing.monitor.PackageTest.suite());
        }

        suite.addTest(jmri.jmrix.can.cbus.PackageTest.suite());

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
