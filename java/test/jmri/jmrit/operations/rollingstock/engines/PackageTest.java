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
        junit.textui.TestRunner.main(testCaseName);
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
        suite.addTest(new junit.framework.JUnit4TestAdapter(BundleTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(EnginesTableFrameTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(EngineEditFrameTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(EngineAttributeEditFrameTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(EngineSetFrameTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(EngineManagerXmlTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(EnginesTableActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(EnginesTableModelTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(ExportEnginesTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(ImportEnginesTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(ImportRosterEnginesTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(ConsistTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(ExportEngineRosterActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(DeleteEngineRosterActionTest.class)); 
        suite.addTest(new junit.framework.JUnit4TestAdapter(ImportEngineActionTest.class)); 
        suite.addTest(new junit.framework.JUnit4TestAdapter(ImportRosterEngineActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(NceConsistEngineActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(ResetEngineMovesActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(PrintEngineRosterActionTest.class));
        return suite;
    }

}
