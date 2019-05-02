package jmri.jmrit.operations.rollingstock.cars.tools;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        CarAttributeActionTest.class,
        CarAttributeEditFrameTest.class,
        CarDeleteAttributeActionTest.class,
        CarLoadAttributeActionTest.class,
        CarLoadEditFrameTest.class,
        DeleteCarRosterActionTest.class,
        EnableDestinationActionTest.class,
        ExportCarRosterActionTest.class,
        ExportCarsTest.class,
        ImportCarRosterActionTest.class,
        ImportCarsTest.class,
        PrintCarLoadsActionTest.class,
        PrintCarRosterActionTest.class,
        ResetCarMovesActionTest.class,
        ResetCheckboxesCarsTableActionTest.class,
        ShowCheckboxesCarsTableActionTest.class
})

/**
 * Tests for the jmrit.operations.rollingstock.cars package
 *
 * @author Bob Coleman
 */
public class PackageTest {
}
