package jmri.jmrit.operations.trains.schedules;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        BundleTest.class,
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
 * Tests for the jmrit.operations.trains.schedules package
 *
 * @author	Bob Coleman
 */
public class PackageTest  {
}
