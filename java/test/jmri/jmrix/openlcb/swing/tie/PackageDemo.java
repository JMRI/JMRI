// PackageDemo.java
package jmri.jmrix.openlcb.swing.tie;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.openlcb package.
 *
 * @author Bob Jacobsen Copyright 2009, 2012
 */
public class PackageDemo extends TestCase {

    public void testDefinitions() {
    }

    // from here down is testing infrastructure
    public PackageDemo(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        apps.tests.AllTest.initLogging();
        String[] testCaseName = {"-noloading", PackageDemo.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrix.openlcb.tie.PackageDemo");

        suite.addTest(TieToolFrameDemo.suite());

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
