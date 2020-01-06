package jmri.jmrit.operations.routes;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        BundleTest.class,
        OperationsRoutesGuiTest.class,
        OperationsRoutesTest.class,
        RouteEditFrameTest.class,
        RouteEditTableModelTest.class,
        RouteLocationTest.class,
        RouteManagerTest.class,
        RouteManagerXmlTest.class,
        RoutesTableActionTest.class,
        RoutesTableFrameTest.class,
        RoutesTableModelTest.class,
        RouteTest.class,
        jmri.jmrit.operations.routes.tools.PackageTest.class,
})

/**
 * Tests for the jmrit.operations.routes package
 *
 * @author Bob Coleman
 */
public class PackageTest {
}
