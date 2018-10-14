package jmri.jmrit.operations.rollingstock;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        OperationsRollingStockTest.class,
        jmri.jmrit.operations.rollingstock.cars.PackageTest.class,
        jmri.jmrit.operations.rollingstock.engines.PackageTest.class,
        BundleTest.class,
        ImportRollingStockTest.class,
        RollingStockAttributeTest.class,
        RollingStockLoggerTest.class,
        RollingStockManagerTest.class,
        RollingStockSetFrameTest.class,
        RollingStockTest.class,
        XmlTest.class,
        RollingStockGroupTest.class,

        // Last test, deletes log file if one exists
        OperationsLoggerTest.class,
})

/**
 * Tests for the jmrit.operations.rollingstock package
 *
 * @author	Bob Coleman
 */
public class PackageTest  {
}
