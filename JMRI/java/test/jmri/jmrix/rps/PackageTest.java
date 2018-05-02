package jmri.jmrix.rps;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        MeasurementTest.class,
        PositionFileTest.class,
        ReadingTest.class,
        EngineTest.class,
        RpsSensorManagerTest.class,
        jmri.jmrix.rps.RpsSensorTest.class,
        jmri.jmrix.rps.RegionTest.class,
        BundleTest.class,
        jmri.jmrix.rps.serial.PackageTest.class,
        jmri.jmrix.rps.configurexml.PackageTest.class,
        jmri.jmrix.rps.aligntable.PackageTest.class,
        jmri.jmrix.rps.reversealign.PackageTest.class,
        RpsPositionIconTest.class,
        jmri.jmrix.rps.rpsmon.PackageTest.class,
        jmri.jmrix.rps.swing.PackageTest.class, // do 2nd to display in front
        jmri.jmrix.rps.csvinput.CsvTest.class, // do 3rd to display in front
        jmri.jmrix.rps.trackingpanel.PackageTest.class, // do 4th to display in front
        // test all algorithms as a bunch
        jmri.jmrix.rps.algorithms.PackageTest.class,
        AlgorithmsTest.class,
        DistributorTest.class,
        ModelTest.class,
        PollingFileTest.class,
        RpsConnectionTypeListTest.class,
        RpsMenuTest.class,
        RpsReporterManagerTest.class,
        RpsSystemConnectionMemoTest.class,
        ReceiverTest.class,
        TransmitterTest.class,
        RpsBlockTest.class,
        RpsReporterTest.class,
})

/**
 * Tests for the jmri.jmrix.rps package.
 *
 * @author Bob Jacobsen Copyright 2006
 */
public class PackageTest  {
}
