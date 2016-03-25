// PackageTest.java
package jmri.jmrit.operations.routes;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmrit.operations.routes package
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
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrit.operations.routes.PackageTest"); // no tests in class itself
        suite.addTest(OperationsRoutesTest.suite());

        // GUI tests start here
        if (!System.getProperty("jmri.headlesstest", "false").equals("true")) {
            suite.addTest(OperationsRoutesGuiTest.suite());
        }

        return suite;
    }

}
