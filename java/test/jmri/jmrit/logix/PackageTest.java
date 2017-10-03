package jmri.jmrit.logix;

import jmri.util.JUnitUtil;
import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Invokes complete set of tests in the jmri.jmrit.logix tree
 *
 * @author	Bob Jacobsen Copyright 2010
 */
public class PackageTest extends TestCase {

    // from here down is testing infrastructure
    public PackageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        apps.tests.Log4JFixture.initLogging();
        String[] testCaseName = {"-noloading", PackageTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrit.logix.PackageTest");   // no tests in this class itself

//		Something wrong in the xsd files?  maybe using -2-9-6 version?
        suite.addTest(new JUnit4TestAdapter(SchemaTest.class));
        suite.addTest(new JUnit4TestAdapter(OBlockTest.class));
        suite.addTest(new JUnit4TestAdapter(OBlockManagerTest.class));
        suite.addTest(new JUnit4TestAdapter(OPathTest.class));
        suite.addTest(new JUnit4TestAdapter(PortalTest.class));
        suite.addTest(new JUnit4TestAdapter(WarrantTest.class));
        suite.addTest(new JUnit4TestAdapter(LogixActionTest.class));
        suite.addTest(new JUnit4TestAdapter(BundleTest.class));
        suite.addTest(new JUnit4TestAdapter(jmri.jmrit.logix.configurexml.PackageTest.class));
        suite.addTest(new JUnit4TestAdapter(NXFrameTest.class)); //formerly NXWarrantTest        
        suite.addTest(LearnWarrantTest.suite());
        suite.addTest(new JUnit4TestAdapter(PortalManagerTest.class));
        suite.addTest(new JUnit4TestAdapter(ThrottleSettingTest.class));
        suite.addTest(new JUnit4TestAdapter(WarrantManagerTest.class));
        suite.addTest(new JUnit4TestAdapter(WarrantPreferencesPanelTest.class));
        suite.addTest(new JUnit4TestAdapter(WarrantPreferencesTest.class));
        suite.addTest(new JUnit4TestAdapter(TrackerTableActionTest.class));
        suite.addTest(new JUnit4TestAdapter(WarrantFrameTest.class));
        suite.addTest(new JUnit4TestAdapter(WarrantTableActionTest.class));
        suite.addTest(new JUnit4TestAdapter(WarrantTableFrameTest.class));
        suite.addTest(new JUnit4TestAdapter(WarrantTableModelTest.class));
        suite.addTest(new JUnit4TestAdapter(LearnThrottleFrameTest.class));
        suite.addTest(new JUnit4TestAdapter(TrackerTest.class));
        suite.addTest(new JUnit4TestAdapter(BlockOrderTest.class));
        suite.addTest(new JUnit4TestAdapter(ControlPanelTest.class));
        suite.addTest(new JUnit4TestAdapter(OpSessionLogTest.class));
        suite.addTest(new JUnit4TestAdapter(SCWarrantTest.class));
        suite.addTest(new JUnit4TestAdapter(EngineerTest.class));
        suite.addTest(new JUnit4TestAdapter(SpeedUtilTest.class));
        suite.addTest(new JUnit4TestAdapter(FunctionPanelTest.class));
        suite.addTest(new JUnit4TestAdapter(WarrantShutdownTaskTest.class));
        suite.addTest(new JUnit4TestAdapter(SpeedProfilePanelTest.class));
        suite.addTest(new JUnit4TestAdapter(RouteFinderTest.class));
        suite.addTest(new JUnit4TestAdapter(MergePromptTest.class));
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() {
        JUnitUtil.setUp();
    }

    @Override
    protected void tearDown() {
        JUnitUtil.tearDown();
    }

}
