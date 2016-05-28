//JmrisTest.java
package jmri.jmris;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Set of tests for the jmri.jmris package
 *
 * @author	Paul Bender Copyright 2010
 */
public class PackageTest extends TestCase {

    // from here down is testing infrastructure
    public PackageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {PackageTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmris.JmrisTest");

        suite.addTest(jmri.jmris.srcp.SRCPTest.suite());
        suite.addTest(jmri.jmris.simpleserver.SimpleServerTest.suite());
        suite.addTest(jmri.jmris.json.JsonServerTest.suite());
        suite.addTest(jmri.jmris.JmriServerTest.suite());
        suite.addTest(jmri.jmris.JmriConnectionTest.suite());
        suite.addTest(jmri.jmris.ServiceHandlerTest.suite());
        suite.addTest(BundleTest.suite());

        if (!System.getProperty("jmri.headlesstest", "false").equals("true")) {
            // put any tests that require a UI here.
            suite.addTest(jmri.jmris.JmriServerFrameTest.suite());
            suite.addTest(jmri.jmris.JmriServerActionTest.suite());
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
