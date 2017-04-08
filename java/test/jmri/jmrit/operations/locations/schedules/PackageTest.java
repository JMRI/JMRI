package jmri.jmrit.operations.locations.schedules;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmrit.operations.locations package
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
        TestSuite suite = new TestSuite("jmri.jmrit.operations.locations.schedules.PackageTest"); // no tests in class itself
        suite.addTest(ScheduleItemTest.suite());
        suite.addTest(ScheduleTest.suite());
        suite.addTest(ScheduleManagerTest.suite());
        suite.addTest(new junit.framework.JUnit4TestAdapter(BundleTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(ScheduleEditFrameGuiTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(ScheduleGuiTests.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(ScheduleCopyActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(ScheduleCopyFrameTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(ScheduleTableModelTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(SchedulesByLoadFrameTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(SchedulesTableFrameTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(SchedulesTableModelTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(XmlTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(ScheduleResetHitsActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(SchedulesByLoadActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(SchedulesResetHitsActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(SchedulesTableActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(ScheduleEditFrameTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(ScheduleOptionsActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(ScheduleOptionsFrameTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(LocationTrackPairTest.class));
        return suite;
    }

}
