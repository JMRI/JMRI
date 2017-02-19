package jmri.jmrit.operations.locations.tools;

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
        TestSuite suite = new TestSuite("jmri.jmrit.operations.locations.tools.PackageTest"); // no tests in class itself
        suite.addTest(new junit.framework.JUnit4TestAdapter(BundleTest.class));
        suite.addTest(PoolTrackGuiTest.suite());
        suite.addTest(new junit.framework.JUnit4TestAdapter(EditCarTypeActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(LocationCopyActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(LocationCopyFrameTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(LocationTrackBlockingOrderActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(LocationTrackBlockingOrderFrameTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(LocationTrackBlockingOrderTableModelTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(LocationsByCarLoadFrameTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(LocationsByCarTypeFrameTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(ModifyLocationsActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(ModifyLocationsCarLoadsActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(ShowTrackMovesActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(ShowTrainsServingLocationFrameTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(TrackDestinationEditFrameTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(TrackLoadEditFrameTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(TrackRoadEditFrameTest.class));
        return suite;
    }

}
