package jmri.jmrit.ussctc;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        jmri.jmrit.ussctc.FollowerTest.class,
        jmri.jmrit.ussctc.FollowerActionTest.class,
        jmri.jmrit.ussctc.OsIndicatorTest.class,
        jmri.jmrit.ussctc.OsIndicatorActionTest.class,
        BasePanelTest.class,
        FollowerFrameTest.class,
        FollowerPanelTest.class,
        OsIndicatorFrameTest.class,
        OsIndicatorPanelTest.class,
        ToolsMenuTest.class,
        
        // new classes last
        jmri.jmrit.ussctc.CombinedLockTest.class,
        jmri.jmrit.ussctc.OccupancyLockTest.class,
        jmri.jmrit.ussctc.TurnoutLockTest.class,
        jmri.jmrit.ussctc.RouteLockTest.class,
        jmri.jmrit.ussctc.TrafficLockTest.class,
        jmri.jmrit.ussctc.TimeLockTest.class,
        jmri.jmrit.ussctc.StationTest.class,
        jmri.jmrit.ussctc.CodeLineTest.class,
        jmri.jmrit.ussctc.CodeButtonTest.class,
        jmri.jmrit.ussctc.PhysicalBellTest.class,
        jmri.jmrit.ussctc.VetoedBellTest.class,
        jmri.jmrit.ussctc.TrafficRelayTest.class,
        jmri.jmrit.ussctc.MaintainerCallSectionTest.class,
        jmri.jmrit.ussctc.TrackCircuitSectionTest.class,
        jmri.jmrit.ussctc.TurnoutSectionTest.class,
        jmri.jmrit.ussctc.SignalHeadSectionTest.class,
})

/**
 * Tests for classes in the jmri.jmrit.ussctc package
 *
 * @author	Bob Jacobsen Copyright 2007
  */
public class PackageTest  {
}
