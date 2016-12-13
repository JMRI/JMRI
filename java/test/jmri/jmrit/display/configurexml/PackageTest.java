package jmri.jmrit.display.configurexml;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * PackageTest.java
 *
 * Description:	Tests for the jmrit.display.configurexml package
 *
 * @author	Bob Jacobsen Copyright 2009, 2014
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
        TestSuite suite = new TestSuite("jmri.jmrit.display.configurexml");   // no tests in this class itself
        suite.addTest(new JUnit4TestAdapter(SchemaTest.class));
        suite.addTest(LoadAndStoreTest.suite());
        suite.addTest(new JUnit4TestAdapter(AnalogClock2DisplayXmlTest.class));
        suite.addTest(new JUnit4TestAdapter(BlockContentsIconXmlTest.class));
        suite.addTest(new JUnit4TestAdapter(IndicatorTrackIconXmlTest.class));
        suite.addTest(new JUnit4TestAdapter(LayoutBlockManagerXmlTest.class));
        suite.addTest(new JUnit4TestAdapter(LayoutTurnoutXmlTest.class));
        suite.addTest(new JUnit4TestAdapter(LayoutTurntableXmlTest.class));
        suite.addTest(new JUnit4TestAdapter(LightIconXmlTest.class));
        suite.addTest(new JUnit4TestAdapter(MemoryComboIconXmlTest.class));
        suite.addTest(new JUnit4TestAdapter(MemoryIconXmlTest.class));
        suite.addTest(new JUnit4TestAdapter(MemoryInputIconXmlTest.class));
        suite.addTest(new JUnit4TestAdapter(MemorySpinnerIconXmlTest.class));
        suite.addTest(new JUnit4TestAdapter(MultiSensorIconXmlTest.class));
        suite.addTest(new JUnit4TestAdapter(PositionableLabelXmlTest.class));
        suite.addTest(new JUnit4TestAdapter(PositionablePointXmlTest.class));
        suite.addTest(new JUnit4TestAdapter(ReporterIconXmlTest.class));
        suite.addTest(new JUnit4TestAdapter(RpsPositionIconXmlTest.class));
        suite.addTest(new JUnit4TestAdapter(SensorIconXmlTest.class));
        suite.addTest(new JUnit4TestAdapter(SignalHeadIconXmlTest.class));
        suite.addTest(new JUnit4TestAdapter(SignalMastIconXmlTest.class));
        suite.addTest(new JUnit4TestAdapter(SlipTurnoutIconXmlTest.class));
        suite.addTest(new JUnit4TestAdapter(TrackSegmentXmlTest.class));
        suite.addTest(new JUnit4TestAdapter(TurnoutIconXmlTest.class));
        return suite;
    }

}
