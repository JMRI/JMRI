package jmri.jmrit.display.layoutEditor;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

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
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(PackageTest.class.getName());

        suite.addTest(new JUnit4TestAdapter(SchemaTest.class));
        suite.addTest(LayoutBlockTest.suite());
        suite.addTest(LayoutBlockManagerTest.suite());
        suite.addTest(BlockValueFileTest.suite());
        suite.addTest(LayoutBlockConnectivityToolsTest.suite());
        suite.addTest(LayoutTurnoutTest.suite());
        suite.addTest(LayoutConnectivityTest.suite());

        suite.addTest(new JUnit4TestAdapter(BundleTest.class));
        suite.addTest(new JUnit4TestAdapter(jmri.jmrit.display.layoutEditor.blockRoutingTable.PackageTest.class));
        suite.addTest(new JUnit4TestAdapter(jmri.jmrit.display.layoutEditor.configurexml.PackageTest.class));
        suite.addTest(LayoutEditorActionTest.suite());
        suite.addTest(new JUnit4TestAdapter(LayoutEditorTest.class));
        suite.addTest(new JUnit4TestAdapter(LayoutEditorToolsTest.class));
        suite.addTest(new JUnit4TestAdapter(LayoutEditorAuxToolsTest.class));
        suite.addTest(new JUnit4TestAdapter(ConnectivityUtilTest.class));
        suite.addTest(new JUnit4TestAdapter(BlockContentsIconTest.class));
        suite.addTest(MultiIconEditorTest.suite());
        suite.addTest(new JUnit4TestAdapter(MultiSensorIconFrameTest.class));
        suite.addTest(new JUnit4TestAdapter(LayoutTurntableTest.class));
        suite.addTest(new JUnit4TestAdapter(LevelXingTest.class));
        suite.addTest(new JUnit4TestAdapter(LayoutSlipTest.class));
        suite.addTest(new JUnit4TestAdapter(MemoryIconTest.class));
        suite.addTest(new JUnit4TestAdapter(PositionablePointTest.class));
        suite.addTest(new JUnit4TestAdapter(TrackNodeTest.class));
        suite.addTest(new JUnit4TestAdapter(TrackSegmentTest.class));
        suite.addTest(new JUnit4TestAdapter(TransitCreationToolTest.class));
        suite.addTest(LayoutEditorWindowTest.suite());
        suite.addTest(LEConnectivityTest.suite());
        suite.addTest(new JUnit4TestAdapter(LayoutEditorFindItemsTest.class));
        return suite;
    }
}
