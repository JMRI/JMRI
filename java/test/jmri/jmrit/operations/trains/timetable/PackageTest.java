package jmri.jmrit.operations.trains.timetable;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmrit.operations.trains.timetable package
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
        TestSuite suite = new TestSuite("jmri.jmrit.operations.trains.timetable.PackageTest"); // no tests in class itself
        suite.addTest(new junit.framework.JUnit4TestAdapter(BundleTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(OperationsTrainsGuiTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(TrainScheduleManagerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(TrainsScheduleEditActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(TrainsScheduleEditFrameTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(TrainsScheduleTableFrameTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(TrainsScheduleTableModelTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(XmlTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(TrainsScheduleActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(TrainScheduleTest.class));
        return suite;
    }

}
