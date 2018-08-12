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
        CombinedLockTest.class,
        OccupancyLockTest.class,
        TurnoutLockTest.class,
        RouteLockTest.class,
        TrafficLockTest.class,
        TimeLockTest.class,
        StationTest.class,
        CodeLineTest.class,
        CodeButtonTest.class,
        PhysicalBellTest.class,
        VetoedBellTest.class,
        TrafficRelayTest.class,
        MaintainerCallSectionTest.class,
        TrackCircuitSectionTest.class,
        TurnoutSectionTest.class,
        SignalHeadSectionTest.class,
        LockTest.class,
})

/**
 * Tests for classes in the jmri.jmrit.ussctc package
 *
 * @author	Bob Jacobsen Copyright 2007
  */
public class PackageTest  {
}
