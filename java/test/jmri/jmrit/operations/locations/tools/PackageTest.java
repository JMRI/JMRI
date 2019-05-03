package jmri.jmrit.operations.locations.tools;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        AlternateTrackFrameTest.class,
        AlternateTrackActionTest.class,
        BundleTest.class,
        ChangeTrackFrameTest.class,
        ChangeTracksFrameTest.class,
        ChangeTracksTypeActionTest.class,
        ChangeTrackTypeActionTest.class,      
        EditCarTypeActionTest.class,
        ExportLocationsRosterActionTest.class,
        ExportLocationsTest.class,
        LocationCopyActionTest.class,
        LocationCopyFrameTest.class,
        LocationTrackBlockingOrderActionTest.class,
        LocationTrackBlockingOrderFrameTest.class,
        LocationTrackBlockingOrderTableModelTest.class,
        LocationsByCarLoadFrameTest.class,
        LocationsByCarTypeFrameTest.class,
        ModifyLocationsActionTest.class,
        ModifyLocationsCarLoadsActionTest.class,
        PoolTrackGuiTest.class,
        ShowTrackMovesActionTest.class,
        ShowTrainsServingLocationFrameTest.class,
        TrackDestinationEditFrameTest.class,
        TrackLoadEditFrameTest.class,
        TrackRoadEditFrameTest.class,
        IgnoreUsedTrackActionTest.class,
        IgnoreUsedTrackFrameTest.class,
        PoolTrackFrameTest.class,
        PoolTrackActionTest.class,
        TrackDestinationEditActionTest.class,
        TrackLoadEditActionTest.class,
        TrackRoadEditActionTest.class,
        TrackEditCommentsActionTest.class,
        TrackEditCommentsFrameTest.class,

        TrackCopyActionTest.class,
        TrackCopyFrameTest.class,
        ShowCarsByLocationActionTest.class,
        PrintSwitchListActionTest.class,
        SetPhysicalLocationActionTest.class,
        SetPhysicalLocationFrameTest.class,
        PrintLocationsActionTest.class,
        ShowTrainsServingLocationActionTest.class,
        PrintLocationsByCarTypesActionTest.class,
})

/**
 * Tests for the jmrit.operations.locations package
 *
 * @author Bob Coleman
 */
public class PackageTest {
}
