package jmri.jmrit.operations.rollingstock.engines;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        EngineTest.class,
        EngineLengthsTest.class,
        EngineTypesTest.class,
        EngineModelsTest.class,
        EngineManagerTest.class,
        XmlTest.class,
        BundleTest.class,
        EnginesTableFrameTest.class,
        EngineEditFrameTest.class,
        EngineSetFrameTest.class,
        EngineManagerXmlTest.class,
        EnginesTableActionTest.class,
        EnginesTableModelTest.class,
        ConsistTest.class,
        jmri.jmrit.operations.rollingstock.engines.tools.PackageTest.class, 
        BundleTest.class,
})

/**
 * Tests for the jmrit.operations.rollingstock package
 *
 * @author	Bob Coleman
 */
public class PackageTest  {
}
