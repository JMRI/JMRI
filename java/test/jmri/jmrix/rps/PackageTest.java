package jmri.jmrix.rps;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.rps package.
 *
 * @author Bob Jacobsen Copyright 2006
 */
public class PackageTest extends TestCase {

    // from here down is testing infrastructure
    public PackageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {PackageTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite("jmri.jmrix.rps.RpsTest");
        suite.addTest(MeasurementTest.suite());
        suite.addTest(PositionFileTest.suite());
        suite.addTest(ReadingTest.suite());
        suite.addTest(EngineTest.suite());
        suite.addTest(new JUnit4TestAdapter(RpsSensorManagerTest.class));
        suite.addTest(jmri.jmrix.rps.RpsSensorTest.suite());
        suite.addTest(jmri.jmrix.rps.RegionTest.suite());
        suite.addTest(jmri.jmrix.rps.TransformTest.suite());
        suite.addTest(new JUnit4TestAdapter(BundleTest.class));
        suite.addTest(new JUnit4TestAdapter(jmri.jmrix.rps.serial.PackageTest.class));
        suite.addTest(new JUnit4TestAdapter(jmri.jmrix.rps.configurexml.PackageTest.class));
        suite.addTest(new JUnit4TestAdapter(jmri.jmrix.rps.aligntable.PackageTest.class));
        suite.addTest(new JUnit4TestAdapter(jmri.jmrix.rps.reversealign.PackageTest.class));
        suite.addTest(new JUnit4TestAdapter(RpsPositionIconTest.class));
        suite.addTest(new JUnit4TestAdapter(jmri.jmrix.rps.rpsmon.PackageTest.class));
        suite.addTest(new JUnit4TestAdapter(jmri.jmrix.rps.swing.PackageTest.class)); // do 2nd to display in front
        suite.addTest(jmri.jmrix.rps.csvinput.CsvTest.suite()); // do 3rd to display in front
        suite.addTest(new JUnit4TestAdapter(jmri.jmrix.rps.trackingpanel.PackageTest.class)); // do 4th to display in front
        // test all algorithms as a bunch
        suite.addTest(new JUnit4TestAdapter(jmri.jmrix.rps.algorithms.PackageTest.class));
        suite.addTest(new JUnit4TestAdapter(AlgorithmsTest.class));
        suite.addTest(new JUnit4TestAdapter(DistributorTest.class));
        suite.addTest(new JUnit4TestAdapter(ModelTest.class));
        suite.addTest(new JUnit4TestAdapter(PollingFileTest.class));
        suite.addTest(new JUnit4TestAdapter(RpsConnectionTypeListTest.class));
        suite.addTest(new JUnit4TestAdapter(RpsMenuTest.class));
        suite.addTest(new JUnit4TestAdapter(RpsReporterManagerTest.class));
        suite.addTest(new JUnit4TestAdapter(RpsSystemConnectionMemoTest.class));
        suite.addTest(new JUnit4TestAdapter(ReceiverTest.class));
        suite.addTest(new JUnit4TestAdapter(TransmitterTest.class));
        suite.addTest(new JUnit4TestAdapter(RpsBlockTest.class));
        suite.addTest(new JUnit4TestAdapter(RpsReporterTest.class));

        return suite;
    }

}
