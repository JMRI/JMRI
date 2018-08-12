package jmri.jmrit.dispatcher;



import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        DispatcherTrainInfoTest.class,
        DispatcherTrainInfoFileTest.class,
        BundleTest.class,
        DispatcherFrameTest.class,
        DispatcherActionTest.class,
        OptionsFileTest.class,
        TrainInfoFileTest.class,
        TrainInfoTest.class,
        ActivateTrainFrameTest.class,
        AutoTrainsFrameTest.class,
        AutoAllocateTest.class,
        AutoTurnoutsTest.class,
        OptionsMenuTest.class,
        ActiveTrainTest.class,
        AllocatedSectionTest.class,
        AllocationRequestTest.class,
        AllocationPlanTest.class,
        AutoActiveTrainTest.class,
        AutoTrainActionTest.class,
})

/**
 * Tests for the jmrit.dispatcher package
 *
 * @author	Dave Duchamp
 */
public class PackageTest {
}
