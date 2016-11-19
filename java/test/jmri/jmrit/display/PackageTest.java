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

        suite.addTest(jmri.jmrit.display.SchemaTest.suite());

        suite.addTest(jmri.jmrit.display.PositionableLabelTest.suite());

        if (!System.getProperty("java.awt.headless", "false").equals("true")) {
            suite.addTest(jmri.jmrit.display.LinkingLabelTest.suite());
            suite.addTest(jmri.jmrit.display.MemoryIconTest.suite());
            suite.addTest(jmri.jmrit.display.MemorySpinnerIconTest.suite());
            suite.addTest(jmri.jmrit.display.PanelEditorTest.suite());
            suite.addTest(jmri.jmrit.display.ReporterIconTest.suite());
            suite.addTest(jmri.jmrit.display.RpsPositionIconTest.suite());
            suite.addTest(jmri.jmrit.display.SensorIconWindowTest.suite());
            suite.addTest(jmri.jmrit.display.SignalMastIconTest.suite());
            suite.addTest(jmri.jmrit.display.SignalSystemTest.suite());
            suite.addTest(jmri.jmrit.display.TurnoutIconWindowTest.suite());
            suite.addTest(jmri.jmrit.display.TurnoutIconTest.suite());
            suite.addTest(jmri.jmrit.display.IndicatorTurnoutIconTest.suite());
            suite.addTest(jmri.jmrit.display.IconEditorWindowTest.suite());
        }

        suite.addTest(jmri.jmrit.display.configurexml.PackageTest.suite());
        suite.addTest(jmri.jmrit.display.layoutEditor.PackageTest.suite());
        suite.addTest(jmri.jmrit.display.panelEditor.PackageTest.suite());
        suite.addTest(new JUnit4TestAdapter(jmri.jmrit.display.palette.PackageTest.class));
        suite.addTest(jmri.jmrit.display.controlPanelEditor.PackageTest.suite());

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

        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }
}
