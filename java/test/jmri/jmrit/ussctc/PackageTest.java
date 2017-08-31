package jmri.jmrit.ussctc;

import jmri.util.JUnitUtil;
import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for classes in the jmri.jmrit.ussctc package
 *
 * @author	Bob Jacobsen Copyright 2007
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
        TestSuite suite = new TestSuite("jmri.jmrit.ussctc.PackageTest");   // no tests in this class itself
        suite.addTest(jmri.jmrit.ussctc.FollowerTest.suite());
        suite.addTest(new JUnit4TestAdapter(jmri.jmrit.ussctc.FollowerActionTest.class));
        suite.addTest(jmri.jmrit.ussctc.OsIndicatorTest.suite());
        suite.addTest(new JUnit4TestAdapter(jmri.jmrit.ussctc.OsIndicatorActionTest.class));
        suite.addTest(new JUnit4TestAdapter(BasePanelTest.class));
        suite.addTest(new JUnit4TestAdapter(FollowerFrameTest.class));
        suite.addTest(new JUnit4TestAdapter(FollowerPanelTest.class));
        suite.addTest(new JUnit4TestAdapter(OsIndicatorFrameTest.class));
        suite.addTest(new JUnit4TestAdapter(OsIndicatorPanelTest.class));
        suite.addTest(new JUnit4TestAdapter(ToolsMenuTest.class));
        
        // new classes last
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrit.ussctc.CombinedLockTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrit.ussctc.OccupancyLockTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrit.ussctc.TurnoutLockTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrit.ussctc.RouteLockTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrit.ussctc.TrafficLockTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrit.ussctc.TimeLockTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrit.ussctc.StationTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrit.ussctc.CodeLineTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrit.ussctc.CodeButtonTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrit.ussctc.PhysicalBellTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrit.ussctc.VetoedBellTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrit.ussctc.TrafficRelayTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrit.ussctc.MaintainerCallSectionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrit.ussctc.TrackCircuitSectionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrit.ussctc.TurnoutSectionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrit.ussctc.SignalHeadSectionTest.class));
        
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() {
        JUnitUtil.setUp();
    }

    @Override
    protected void tearDown() {
        JUnitUtil.tearDown();
    }

}
