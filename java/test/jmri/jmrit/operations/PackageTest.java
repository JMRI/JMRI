package jmri.jmrit.operations;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        jmri.jmrit.operations.setup.PackageTest.class,
        jmri.jmrit.operations.rollingstock.PackageTest.class,
        jmri.jmrit.operations.routes.PackageTest.class,
        jmri.jmrit.operations.trains.PackageTest.class,  // fixed references to Swing, 10/10/2012
        jmri.jmrit.operations.router.PackageTest.class,  // fixed references to Swing, 10/10/2012
        jmri.jmrit.operations.locations.PackageTest.class, // fixed references to Swing, 10/10/2012
        jmri.jmrit.operations.automation.PackageTest.class,

        XmlLoadTest.class, // no tests in class itself
        BundleTest.class, 
        CommonConductorYardmasterPanelTest.class, 
//        jmri.jmrit.operations.locations.PackageTest.class, // fixed references to Swing, 10/10/2012
        OperationsFrameTest.class,
        OperationsMenuTest.class,
        OperationsPanelTest.class,
        OpsPropertyChangeListenerTest.class,
        OperationsManagerTest.class,
        ExceptionContextTest.class,
        ExceptionDisplayFrameTest.class,
        UnexpectedExceptionContextTest.class,
})

/**
 * Tests for the jmrit.operations package
 *
 * @author	Bob Coleman
 */
public class PackageTest {
}
