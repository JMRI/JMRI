package jmri.jmrix.rps.swing;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Tests for the jmri.jmrix.rps.swing package.
 *
 * @author Bob Jacobsen Copyright 2008
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    AffineEntryPanelTest.class,
    jmri.jmrix.rps.swing.polling.PackageTest.class,
    jmri.jmrix.rps.swing.debugger.PackageTest.class,
    BundleTest.class,
    jmri.jmrix.rps.swing.debugger.DebuggerTest.class,
    jmri.jmrix.rps.swing.soundset.PackageTest.class,
    CsvExportActionTest.class,
    CsvExportMeasurementActionTest.class,
    LoadStorePanelTest.class,
    RpsComponentFactoryTest.class
})
public class PackageTest {
}
