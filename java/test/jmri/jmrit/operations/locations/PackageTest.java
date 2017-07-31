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
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrit.operations.locations.PackageTest"); // no tests in class itself
        suite.addTest(LocationTest.suite());
        suite.addTest(XmlTest.suite());
        suite.addTest(TrackTest.suite());
        suite.addTest(OperationsPoolTest.suite());
        suite.addTest(new junit.framework.JUnit4TestAdapter(BundleTest.class));

        suite.addTest(jmri.jmrit.operations.locations.tools.PackageTest.suite());
        suite.addTest(jmri.jmrit.operations.locations.schedules.PackageTest.suite());
        suite.addTest(new junit.framework.JUnit4TestAdapter(InterchangeEditFrameTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(LocationEditFrameTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(SidingEditFrameTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(StagingEditFrameTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(YardEditFrameTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(YardmasterPanelTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(InterchangeTableModelTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(LocationManagerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(LocationManagerXmlTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(LocationsTableActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(LocationsTableFrameTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(LocationsTableModelTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(SpurEditFrameTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(SpurTableModelTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(StagingTableModelTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(TrackEditFrameTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(TrackTableModelTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(YardTableModelTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(YardmasterByTrackActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(YardmasterByTrackPanelTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(YardmasterByTrackFrameTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(YardmasterFrameTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(PoolTest.class));
        return suite;
    }

}
