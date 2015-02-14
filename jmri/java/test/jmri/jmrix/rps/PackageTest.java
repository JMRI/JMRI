// PackageTest.java

package jmri.jmrix.rps;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.rps package.
 * @author      Bob Jacobsen  Copyright 2006
 * @version   $Revision$
 */
public class PackageTest extends TestCase {

    // from here down is testing infrastructure

    public PackageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {PackageTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite("jmri.jmrix.rps.RpsTest");
        suite.addTest(MeasurementTest.suite());
        suite.addTest(PositionFileTest.suite());
        suite.addTest(ReadingTest.suite());
        suite.addTest(EngineTest.suite());
        suite.addTest(jmri.jmrix.rps.RpsSensorManagerTest.suite());
        suite.addTest(jmri.jmrix.rps.RpsSensorTest.suite());
        suite.addTest(jmri.jmrix.rps.RegionTest.suite());
        suite.addTest(jmri.jmrix.rps.TransformTest.suite());

        if (!System.getProperty("jmri.headlesstest","false").equals("true")) {
            suite.addTest(jmri.jmrix.rps.reversealign.AlignmentPanelTest.suite());
            suite.addTest(jmri.jmrix.rps.serial.SerialAdapterTest.suite());
            suite.addTest(RpsPositionIconTest.suite());
            suite.addTest(jmri.jmrix.rps.rpsmon.RpsMonTest.suite());
            suite.addTest(jmri.jmrix.rps.swing.SwingTest.suite()); // do 2nd to display in front
            suite.addTest(jmri.jmrix.rps.csvinput.CsvTest.suite()); // do 3rd to display in front
            suite.addTest(jmri.jmrix.rps.trackingpanel.TrackingPanelTest.suite()); // do 4th to display in front
        }
        
        // test all algorithms as a bunch
        suite.addTest(AlgorithmsTest.suite());

        return suite;
    }

}
