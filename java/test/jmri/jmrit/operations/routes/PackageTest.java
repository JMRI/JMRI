package jmri.jmrit.operations.routes;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmrit.operations.routes package
 *
 * @author	Bob Coleman
 */
public class PackageTest extends TestCase {

    // from here down is testing infrastructure
    public PackageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", PackageTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrit.operations.routes.PackageTest"); // no tests in class itself
        suite.addTest(OperationsRoutesTest.suite());
        suite.addTest(new junit.framework.JUnit4TestAdapter(BundleTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(OperationsRoutesGuiTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(RouteCopyFrameTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(RouteEditFrameTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(RouteEditTableModelTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(RouteManagerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(RouteManagerXmlTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(RoutesTableActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(RoutesTableFrameTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(RoutesTableModelTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(SetTrainIconPositionFrameTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(XmlTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(RouteCopyActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(SetTrainIconPositionActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(RouteLocationTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(RouteTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(SetTrainIconRouteActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(SetTrainIconRouteFrameTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(PrintRoutesActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(PrintRouteActionTest.class));
        return suite;
    }

}
