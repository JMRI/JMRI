package jmri.jmrit.operations.trains;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        TrainManagerTest.class,
        TrainTest.class,
        TrainCommonTest.class,
        TrainBuilderGuiTest.class,
        TrainBuilderTest.class,
        XmlTest.class,
        BundleTest.class,
        jmri.jmrit.operations.trains.tools.PackageTest.class,
        jmri.jmrit.operations.trains.excel.PackageTest.class,
        jmri.jmrit.operations.trains.schedules.PackageTest.class,
        jmri.jmrit.operations.trains.configurexml.PackageTest.class,
        BuildFailedExceptionTest.class,
        TrainConductorPanelTest.class, 
        TrainCsvCommonTest.class,
        TrainCsvSwitchListsTest.class,
        TrainEditBuildOptionsFrameTest.class,
        TrainLoadOptionsFrameTest.class,
        TrainLoggerTest.class,
        TrainManagerXmlTest.class,
        TrainManifestHeaderTextTest.class,
        TrainManifestTextTest.class,
        TrainPrintUtilitiesTest.class,
        TrainRoadOptionsFrameTest.class,
        TrainSwitchListEditFrameTest.class,
        TrainSwitchListTextTest.class,
        TrainSwitchListsTest.class,
        TrainUtilitiesTest.class,
        TrainsTableActionTest.class,
        TrainsTableFrameTest.class,
        TrainsTableModelTest.class,
        JsonManifestTest.class,
        TrainConductorActionTest.class,
        TrainConductorFrameTest.class,
        TrainCsvManifestTest.class,
        TrainEditFrameTest.class,
        TrainManifestTest.class,
        TrainEditBuildOptionsActionTest.class,
        TrainLoadOptionsActionTest.class,
        TrainRoadOptionsActionTest.class,
        TrainIconTest.class,
        TrainIconAnimationTest.class,
})

/**
 * Tests for the jmrit.operations.trains package
 *
 * @author	Bob Coleman
 */
public class PackageTest  {
}
