package jmri.jmrit.operations.rollingstock.engines;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        EngineTest.class,
        EngineLengthsTest.class,
        EngineTypesTest.class,
        EngineModelsTest.class,
        NceConsistEnginesTest.class,
        EngineManagerTest.class,
        XmlTest.class,
        BundleTest.class,
        EnginesTableFrameTest.class,
        EngineEditFrameTest.class,
        EngineAttributeEditFrameTest.class,
        EngineSetFrameTest.class,
        EngineManagerXmlTest.class,
        EnginesTableActionTest.class,
        EnginesTableModelTest.class,
        ExportEnginesTest.class,
        ImportEnginesTest.class,
        ImportRosterEnginesTest.class,
        ConsistTest.class,
        ExportEngineRosterActionTest.class,
        DeleteEngineRosterActionTest.class, 
        ImportEngineActionTest.class, 
        ImportRosterEngineActionTest.class,
        NceConsistEngineActionTest.class,
        ResetEngineMovesActionTest.class,
        PrintEngineRosterActionTest.class,
})

/**
 * Tests for the jmrit.operations.rollingstock package
 *
 * @author	Bob Coleman
 */
public class PackageTest  {
}
