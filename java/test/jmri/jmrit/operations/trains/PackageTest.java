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
        suite.addTest(new junit.framework.JUnit4TestAdapter(OperationsTrainsGuiTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(BuildFailedExceptionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(TrainConductorPanelTest.class)); 
        suite.addTest(new junit.framework.JUnit4TestAdapter(TrainCsvCommonTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(TrainCsvSwitchListsTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(TrainEditBuildOptionsFrameTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(TrainLoadOptionsFrameTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(TrainLoggerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(TrainManagerXmlTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(TrainManifestHeaderTextTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(TrainManifestTextTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(TrainPrintUtilitiesTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(TrainRoadOptionsFrameTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(TrainSwitchListEditFrameTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(TrainSwitchListTextTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(TrainSwitchListsTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(TrainUtilitiesTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(TrainsTableActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(TrainsTableFrameTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(TrainsTableModelTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(JsonManifestTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(TrainConductorActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(TrainConductorFrameTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(TrainCsvManifestTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(TrainEditFrameTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(TrainManifestTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(TrainEditBuildOptionsActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(TrainLoadOptionsActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(TrainRoadOptionsActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(TrainIconTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(TrainIconAnimationTest.class));
        return suite;
    }

}
