package jmri.jmrit.display;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmrit.display package
 *
 * @author	Bob Jacobsen Copyright 2008, 2009, 2010, 2015
 */
public class PackageTest extends TestCase {

    // from here down is testing infrastructure
    public PackageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        apps.tests.AllTest.initLogging();
        String[] testCaseName = {"-noloading", PackageTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrit.display");   // no tests in this class itself
        suite.addTest(new JUnit4TestAdapter(SchemaTest.class));
        suite.addTest(PositionableLabelTest.suite());
        suite.addTest(LinkingLabelTest.suite());
        suite.addTest(MemoryIconTest.suite());
        suite.addTest(MemorySpinnerIconTest.suite());
        suite.addTest(new JUnit4TestAdapter(PanelEditorTest.class));
        suite.addTest(ReporterIconTest.suite());
        suite.addTest(RpsPositionIconTest.suite());
        suite.addTest(SensorIconWindowTest.suite());
        suite.addTest(SignalMastIconTest.suite());
        suite.addTest(new JUnit4TestAdapter(SignalSystemTest.class));
        suite.addTest(TurnoutIconWindowTest.suite());
        suite.addTest(TurnoutIconTest.suite());
        suite.addTest(IndicatorTurnoutIconTest.suite());
        suite.addTest(IconEditorWindowTest.suite());
        suite.addTest(new JUnit4TestAdapter(jmri.jmrit.display.configurexml.PackageTest.class));
        suite.addTest(jmri.jmrit.display.switchboardEditor.PackageTest.suite());
        suite.addTest(jmri.jmrit.display.layoutEditor.PackageTest.suite());
        suite.addTest(jmri.jmrit.display.panelEditor.PackageTest.suite());
        suite.addTest(new JUnit4TestAdapter(jmri.jmrit.display.palette.PackageTest.class));
        suite.addTest(new JUnit4TestAdapter(jmri.jmrit.display.controlPanelEditor.PackageTest.class));
        suite.addTest(new JUnit4TestAdapter(BundleTest.class));
        suite.addTest(new JUnit4TestAdapter(SensorTextEditTest.class));
        suite.addTest(new JUnit4TestAdapter(AnalogClock2DisplayTest.class));
        suite.addTest(new JUnit4TestAdapter(BlockContentsIconTest.class));
        suite.addTest(new JUnit4TestAdapter(CoordinateEditTest.class));
        suite.addTest(new JUnit4TestAdapter(IconAdderTest.class));
        suite.addTest(new JUnit4TestAdapter(IndicatorTrackIconTest.class));
        suite.addTest(new JUnit4TestAdapter(IndicatorTrackPathsTest.class));
        suite.addTest(new JUnit4TestAdapter(LightIconTest.class));
        suite.addTest(new JUnit4TestAdapter(LocoIconTest.class));
        suite.addTest(new JUnit4TestAdapter(MemoryComboIconTest.class));
        suite.addTest(new JUnit4TestAdapter(MemoryIconCoordinateEditTest.class));
        suite.addTest(new JUnit4TestAdapter(MultiIconEditorTest.class));
        suite.addTest(new JUnit4TestAdapter(MultiSensorIconTest.class));
        suite.addTest(new JUnit4TestAdapter(MultiSensorIconAdderTest.class));
        suite.addTest(new JUnit4TestAdapter(PositionableIconTest.class));
        suite.addTest(new JUnit4TestAdapter(SensorIconTest.class));
        suite.addTest(new JUnit4TestAdapter(SignalHeadIconTest.class));
        suite.addTest(new JUnit4TestAdapter(SlipIconAdderTest.class));
        suite.addTest(new JUnit4TestAdapter(SlipTurnoutIconTest.class));
        suite.addTest(new JUnit4TestAdapter(SlipTurnoutTextEditTest.class));
        suite.addTest(new JUnit4TestAdapter(NewPanelActionTest.class));
        suite.addTest(new JUnit4TestAdapter(MemoryInputIconTest.class));
        suite.addTest(new JUnit4TestAdapter(PositionableJComponentTest.class));
        suite.addTest(new JUnit4TestAdapter(PositionableJPanelTest.class));
        suite.addTest(new JUnit4TestAdapter(ToolTipTest.class));
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
}
