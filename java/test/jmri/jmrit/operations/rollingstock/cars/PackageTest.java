package jmri.jmrit.operations.rollingstock.cars;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmrit.operations.rollingstock.cars package
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
        TestSuite suite = new TestSuite("jmri.jmrit.operations.rollingstock.cars.PackageTest"); // no tests in class itself
        suite.addTest(CarsTest.suite());
        suite.addTest(CarColorsTest.suite());
        suite.addTest(CarTypesTest.suite());
        suite.addTest(CarLengthsTest.suite());
        suite.addTest(CarOwnersTest.suite());
        suite.addTest(CarRoadsTest.suite());
        suite.addTest(CarLoadsTest.suite());
        suite.addTest(KernelTest.suite());
        suite.addTest(CarManagerTest.suite());
        suite.addTest(XmlTest.suite());
        suite.addTest(BundleTest.suite());

        // GUI tests start here
        if (!System.getProperty("jmri.headlesstest", "false").equals("true")) {
            suite.addTest(CarsTableFrameTest.suite());
            suite.addTest(CarEditFrameTest.suite());
            suite.addTest(CarAttributeEditFrameTest.suite());
            suite.addTest(CarLoadEditFrameTest.suite());
            suite.addTest(CarSetFrameTest.suite());
        }

        return suite;
    }

}
