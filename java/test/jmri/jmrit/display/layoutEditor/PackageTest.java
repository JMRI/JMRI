package jmri.jmrit.display.layoutEditor;

import junit.framework.*;

/**
 * Tests for the jmrit.display.layoutEditor package
 *
 * @author	Bob Jacobsen Copyright 2008, 2009, 2010
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
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(PackageTest.class.getName());

        suite.addTest(SchemaTest.suite());
        suite.addTest(LayoutBlockTest.suite());
        suite.addTest(LayoutBlockManagerTest.suite());
        suite.addTest(BlockValueFileTest.suite());
        suite.addTest(LayoutBlockConnectivityToolsTest.suite());
        suite.addTest(LayoutTurnoutTest.suite());
        suite.addTest(LayoutConnectivityTest.suite());

        if (!System.getProperty("jmri.headlesstest", "false").equals("true")) {
            suite.addTest(LayoutEditorActionTest.suite());
            suite.addTest(LayoutEditorTest.suite());
            suite.addTest(LayoutEditorToolsTest.suite());
            suite.addTest(LayoutEditorAuxToolsTest.suite());
            suite.addTest(ConnectivityUtilTest.suite());
            suite.addTest(BlockContentsIconTest.suite());
            suite.addTest(MultiIconEditorTest.suite());
            suite.addTest(MultiSensorIconFrameTest.suite());
            suite.addTest(LayoutTurntableTest.suite());
            suite.addTest(LevelXingTest.suite());
            suite.addTest(LayoutSlipTest.suite());
            suite.addTest(MemoryIconTest.suite());
            suite.addTest(PositionablePointTest.suite());
            suite.addTest(TrackNodeTest.suite());
            suite.addTest(TrackSegmentTest.suite());
            suite.addTest(LayoutEditorWindowTest.suite());
            suite.addTest(LEConnectivityTest.suite());
        }

        return suite;
    }
}
