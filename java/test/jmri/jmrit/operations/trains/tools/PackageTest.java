package jmri.jmrit.operations.trains.tools;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmrit.operations.trains.tools package
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
        TestSuite suite = new TestSuite("jmri.jmrit.operations.trains.tools.PackageTest"); // no tests in class itself
        suite.addTest(new junit.framework.JUnit4TestAdapter(BundleTest.class));

        suite.addTest(new junit.framework.JUnit4TestAdapter(OperationsTrainsGuiTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(ChangeDepartureTimesFrameTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(ExportTrainRosterActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(ExportTrainsTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(ShowCarsInTrainFrameTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(TrainByCarTypeFrameTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(TrainManifestOptionFrameTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(TrainScriptFrameTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(TrainsScriptFrameTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(TrainsByCarTypeActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(TrainsByCarTypeFrameTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(TrainsScriptFrameTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(TrainsTableSetColorActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(TrainsTableSetColorFrameTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(TrainCopyActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(ChangeDepartureTimesActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(TrainCopyFrameTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(TrainByCarTypeActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(ShowCarsInTrainActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(PrintSavedTrainManifestActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(PrintTrainActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(PrintTrainsActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(PrintTrainBuildReportActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(PrintTrainManifestActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(PrintTrainsByCarTypesActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(TrainManifestOptionActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(TrainScriptActionTest.class));
        return suite;
    }

}
