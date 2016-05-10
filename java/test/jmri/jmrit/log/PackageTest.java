package jmri.jmrit.log;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.LoggerFactory;

/**
 * Invokes complete set of tests in the jmri.jmrit.log tree
 *
 * @author	Bob Jacobsen Copyright 2003, 2010
 */
public class PackageTest extends TestCase {

    public void testShow() {
        LoggerFactory.getLogger("jmri.jmrix");
        LoggerFactory.getLogger("apps.foo");
        LoggerFactory.getLogger("jmri.util");

        if (!System.getProperty("jmri.headlesstest", "false").equals("true")) {

            try {
                new jmri.util.swing.JmriNamedPaneAction("Log4J Tree",
                        new jmri.util.swing.sdi.JmriJFrameInterface(),
                        "jmri.jmrit.log.Log4JTreePane").actionPerformed(null);
            } catch (Exception e) {
            }
        }
    }

    // from here down is testing infrastructure
    public PackageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", PackageTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(PackageTest.class);
        suite.addTest(BundleTest.suite());
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
