package jmri.jmrit.operations.trains.excel;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmrit.operations.trains.excel package
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
        TestSuite suite = new TestSuite("jmri.jmrit.operations.trains.excel.PackageTest"); // no tests in class itself
        suite.addTest(new junit.framework.JUnit4TestAdapter(BundleTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(SetupExcelProgramFrameTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(SetupExcelProgramManifestFrameTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(SetupExcelProgramSwitchListFrameTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(TrainCustomManifestTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(TrainCustomSwitchListTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(XmlTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(SetupExcelProgramFrameActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(SetupExcelProgramSwitchListFrameActionTest.class));
        return suite;
    }

}
