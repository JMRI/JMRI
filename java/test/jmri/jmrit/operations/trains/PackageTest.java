package jmri.jmrit.operations.trains;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmrit.operations.trains package
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
        TestSuite suite = new TestSuite("jmri.jmrit.operations.trains.PackageTest"); // no tests in class itself
        suite.addTest(TrainManagerTest.suite());
        suite.addTest(TrainTest.suite());
        suite.addTest(TrainCommonTest.suite());
        suite.addTest(TrainBuilderTest.suite());
        suite.addTest(XmlTest.suite());
        suite.addTest(new junit.framework.JUnit4TestAdapter(BundleTest.class));
        suite.addTest(jmri.jmrit.operations.trains.tools.PackageTest.suite());
        suite.addTest(jmri.jmrit.operations.trains.excel.PackageTest.suite());
        suite.addTest(jmri.jmrit.operations.trains.timetable.PackageTest.suite());
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrit.operations.trains.configurexml.PackageTest.class));
        suite.addTest(OperationsTrainsGuiTest.suite());
        suite.addTest(new junit.framework.JUnit4TestAdapter(BuildFailedExceptionTest.class));

        return suite;
    }

}
