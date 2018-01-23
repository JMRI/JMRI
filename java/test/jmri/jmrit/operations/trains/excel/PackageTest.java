package jmri.jmrit.operations.trains.excel;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        BundleTest.class,
        SetupExcelProgramFrameTest.class,
        SetupExcelProgramManifestFrameTest.class,
        SetupExcelProgramSwitchListFrameTest.class,
        TrainCustomManifestTest.class,
        TrainCustomSwitchListTest.class,
        XmlTest.class,
        SetupExcelProgramFrameActionTest.class,
        SetupExcelProgramSwitchListFrameActionTest.class,
})

/**
 * Tests for the jmrit.operations.trains.excel package
 *
 * @author	Bob Coleman
 */
public class PackageTest  {
}
