// PackageTest.java
package jmri.jmrit.operations.locations;

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
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrit.operations.locations.PackageTest"); // no tests in class itself
        suite.addTest(LocationTest.suite());
        suite.addTest(XmlTest.suite());
        suite.addTest(ScheduleItemTest.suite());
        suite.addTest(ScheduleTest.suite());
        suite.addTest(ScheduleManagerTest.suite());
        suite.addTest(TrackTest.suite());
        suite.addTest(OperationsPoolTest.suite());

        // GUI tests start here
        if (!System.getProperty("jmri.headlesstest", "false").equals("true")) {
            suite.addTest(InterchangeEditFrameTest.suite());
            suite.addTest(LocationEditFrameTest.suite());
            suite.addTest(LocationTableFrameTest.suite());
            suite.addTest(ScheduleEditFrameTest.suite());
            suite.addTest(ScheduleTableFrameTest.suite());
            suite.addTest(SidingEditFrameTest.suite());
            suite.addTest(StagingEditFrameTest.suite());
            suite.addTest(YardEditFrameTest.suite());
            suite.addTest(PoolTrackGuiTest.suite());
        }

        return suite;
    }

}
