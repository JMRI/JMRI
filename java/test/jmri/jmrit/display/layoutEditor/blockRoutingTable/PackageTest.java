package jmri.jmrit.display.layoutEditor.blockRoutingTable;

import junit.framework.*;

/**
 * Tests for the jmrit.display.layoutEditor.blockRouingTable package
 *
 * @author	Bob Jacobsen Copyright 2008, 2009, 2010
 */
public class PackageTest extends TestCase {

    // from here down is testing infrastructure
    public PackageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        apps.tests.Log4JFixture.initLogging();
        
        String[] testCaseName = {"-noloading", PackageTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(PackageTest.class.getName());

        suite.addTest(BundleTest.suite());

        if (!System.getProperty("jmri.headlesstest", "false").equals("true")) {
        }

        return suite;
    }
}
