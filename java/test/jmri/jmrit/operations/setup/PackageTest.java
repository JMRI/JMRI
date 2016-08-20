package jmri.jmrit.operations.setup;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmrit.operations.setup package
 *
 * @author	Bob Coleman
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
        TestSuite suite = new TestSuite("jmri.jmrit.operations.setup.PackageTest"); // no tests in class itself
        suite.addTest(OperationsSetupTest.suite());
        suite.addTest(BundleTest.suite());

        // GUI tests start here
        if (!System.getProperty("jmri.headlesstest", "false").equals("true")) {
            suite.addTest(OperationsSetupGuiTest.suite());
        }

        return suite;
    }

}
