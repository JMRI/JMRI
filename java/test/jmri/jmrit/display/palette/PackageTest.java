package jmri.jmrit.display.palette;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmrit.display.paneleditor package
 *
 * @author	Bob Jacobsen Copyright 2008, 2009, 2010, 2015
 */
public class PackageTest extends TestCase {

    // from here down is testing infrastructure
    public PackageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        apps.tests.AllTest.initLogging();
        String[] testCaseName = {"-noloading", PackageTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrit.display.palette");   // no tests in this class itself



        if (!System.getProperty("java.awt.headless", "false").equals("true")) {
            suite.addTest(ItemPaletteTest.suite());
        }

        suite.addTest(new junit.framework.JUnit4TestAdapter(BundleTest.class));

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
