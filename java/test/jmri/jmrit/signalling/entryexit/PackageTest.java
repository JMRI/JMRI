package jmri.jmrit.signalling.entryexit;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
   BundleTest.class,
   StackNXPanelTest.class,
   PointDetailsTest.class,
   ManuallySetRouteTest.class,
   SourceTest.class,
   DestinationPointsTest.class,

})
/**
 * Invokes complete set of tests in the jmri.jmrit.signalling.entryexit tree
 *
 * @author	Bob Jacobsen Copyright 2001, 2003, 2012
 */
<<<<<<< HEAD
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
        TestSuite suite = new TestSuite("jmri.jmrit.signalling.entryexit.PackageTest");   // no tests in this class itself

        suite.addTest(new junit.framework.JUnit4TestAdapter(BundleTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(StackNXPanelTest.class));

        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    @Override
    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }
=======
public class PackageTest {
>>>>>>> JMRI/master
}
