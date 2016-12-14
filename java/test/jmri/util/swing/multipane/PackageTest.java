package jmri.util.swing.multipane;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Invokes complete set of tests in the jmri.util tree
 *
 * @author	Bob Jacobsen Copyright 2003
 */
public class PackageTest extends TestCase {

    // from here down is testing infrastructure
    public PackageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", PackageTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(PackageTest.class.getName());

        suite.addTest(MultiJfcUnitTest.suite());
        suite.addTest(new JUnit4TestAdapter(MultiPaneWindowTest.class));

        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() throws Exception {
        super.setUp();
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
