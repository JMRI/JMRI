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
       
        BundleTest.class, 
        CommonConductorYardmasterPanelTest.class, 
        OperationsFrameTest.class,
        OperationsManagerTest.class,
        OperationsMenuTest.class,
        OperationsPanelTest.class,
        OpsPropertyChangeListenerTest.class,
        XmlLoadTest.class, // no tests in class itself
})

/**
 * Tests for the jmrit.operations package
 *
 * @author	Bob Coleman
 */
public class PackageTest {
}
