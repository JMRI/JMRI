package jmri.jmrit.operations.locations;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        LocationTest.class,
        XmlTest.class,
        TrackTest.class,
        OperationsPoolTest.class,
        BundleTest.class,
        jmri.jmrit.operations.locations.tools.PackageTest.class,
        jmri.jmrit.operations.locations.schedules.PackageTest.class,
        InterchangeEditFrameTest.class,
        LocationEditFrameTest.class,
        SidingEditFrameTest.class,
        StagingEditFrameTest.class,
        YardEditFrameTest.class,
        YardmasterPanelTest.class,
        InterchangeTableModelTest.class,
        LocationManagerTest.class,
        LocationManagerXmlTest.class,
        LocationsTableActionTest.class,
        LocationsTableFrameTest.class,
        LocationsTableModelTest.class,
        SpurEditFrameTest.class,
        SpurTableModelTest.class,
        StagingTableModelTest.class,
        TrackEditFrameTest.class,
        TrackTableModelTest.class,
        YardTableModelTest.class,
        YardmasterByTrackActionTest.class,
        YardmasterByTrackPanelTest.class,
        YardmasterByTrackFrameTest.class,
        YardmasterFrameTest.class,
        PoolTest.class,
})

/**
 * Tests for the jmrit.operations.locations package
 *
 * @author	Bob Coleman
 */
public class PackageTest  {
}
