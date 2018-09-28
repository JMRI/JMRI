package jmri.jmrit.operations.locations.schedules;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        ScheduleItemTest.class,
        ScheduleTest.class,
        ScheduleManagerTest.class,
        BundleTest.class,
        ExportSchedulesActionTest.class,
        ExportSchedulesTest.class,
        ScheduleEditFrameGuiTest.class,
        ScheduleGuiTests.class,
        ScheduleCopyActionTest.class,
        ScheduleCopyFrameTest.class,
        ScheduleTableModelTest.class,
        SchedulesByLoadFrameTest.class,
        SchedulesTableFrameTest.class,
        SchedulesTableModelTest.class,
        XmlTest.class,
        ScheduleResetHitsActionTest.class,
        SchedulesByLoadActionTest.class,
        SchedulesResetHitsActionTest.class,
        SchedulesTableActionTest.class,
        ScheduleEditFrameTest.class,
        ScheduleOptionsActionTest.class,
        ScheduleOptionsFrameTest.class,
        LocationTrackPairTest.class,
})

/**
 * Tests for the jmrit.operations.locations package
 *
 * @author	Bob Coleman
 */
public class PackageTest  {
}
