package jmri.jmrit.operations.rollingstock.engines;

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
        TestSuite suite = new TestSuite("jmri.jmrit.operations.rollingstock.engines.PackageTest"); // no tests in class itself
        suite.addTest(EngineTest.suite());
        suite.addTest(EngineLengthsTest.suite());
        suite.addTest(EngineTypesTest.suite());
        suite.addTest(EngineModelsTest.suite());
        suite.addTest(NceConsistEnginesTest.suite());
        suite.addTest(EngineManagerTest.suite());
        suite.addTest(XmlTest.suite());

        // GUI tests start here
        if (!System.getProperty("jmri.headlesstest", "false").equals("true")) {
            suite.addTest(EnginesTableFrameTest.suite());
            suite.addTest(EngineEditFrameTest.suite());
            suite.addTest(EngineAttributeEditFrameTest.suite());
            suite.addTest(EngineSetFrameTest.suite());
        }

        return suite;
    }

}
