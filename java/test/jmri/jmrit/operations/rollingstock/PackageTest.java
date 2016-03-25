// PackageTest.java
package jmri.jmrit.operations.rollingstock;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmrit.operations.rollingstock package
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
        TestSuite suite = new TestSuite("jmri.jmrit.operations.rollingstock.PackageTest"); // no tests in class itself
        suite.addTest(OperationsRollingStockTest.suite());
        suite.addTest(jmri.jmrit.operations.rollingstock.cars.PackageTest.suite());
        suite.addTest(jmri.jmrit.operations.rollingstock.engines.PackageTest.suite());


        // Last test, deletes log file if one exists
        suite.addTest(OperationsLoggerTest.suite());

        return suite;
    }

}
