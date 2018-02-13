package jmri.jmrit.operations.trains.timetable;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        BundleTest.class,
        OperationsTrainsGuiTest.class,
        TrainScheduleManagerTest.class,
        TrainsScheduleEditActionTest.class,
        TrainsScheduleEditFrameTest.class,
        TrainsScheduleTableFrameTest.class,
        TrainsScheduleTableModelTest.class,
        XmlTest.class,
        TrainsScheduleActionTest.class,
        TrainScheduleTest.class,
})

/**
 * Tests for the jmrit.operations.trains.timetable package
 *
 * @author	Bob Coleman
 */
public class PackageTest  {
}
